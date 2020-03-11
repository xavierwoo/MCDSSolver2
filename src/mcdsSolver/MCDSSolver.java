package mcdsSolver;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MCDSSolver {

    Random random = new Random();

    private Set<Vertex> X_star;
    private Set<Vertex> X_plus;
    private Set<Vertex> X_minu;

    private Set<Vertex> min_X_star = null;
    private int min_X_star_iter_count = 0;

    private final UndirectedGraph<Vertex, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

    private int iter_count = 0;
    private int f;
    private int ever_best_f;

    private int tabu_length = 10;
    private int base_tabu_length = 1;
    private double perturb_base_ratio = 0.3;
    private int fail_improve_count_max = 2000;
    private double time_limit = 3000;

    private long start_time;

    private double time = -1;

    public MCDSSolver(String instance) throws IOException {
        long s_time = System.currentTimeMillis();
        BufferedReader in;
        in = new BufferedReader(new FileReader(instance));
        String line = in.readLine();
        String[] r = line.split(" ");
        int num_edge = Integer.parseInt(r[1]);

        for(int i=0; i<num_edge; ++i) {
            line = in.readLine();
            r = line.split(" ");
            Vertex source = get_vertex(Integer.parseInt(r[0]));
            Vertex target = get_vertex(Integer.parseInt(r[1]));

            if (graph.addEdge(source, target) == null) {
                System.out.println("The instance have duplicate edges");
            }
        }
        System.out.println("Instance loaded, using time " + (System.currentTimeMillis() - s_time)/1000.0);
    }

    private Vertex get_vertex(int vIndex){
        Vertex v = graph.vertexSet().stream().filter(a->a.index==vIndex).findAny().orElse(new Vertex(vIndex));
        graph.addVertex(v);
        return v;
    }


    private Set<Vertex> get_X_star_sub_F(Set<Vertex> F){
        var X_sub_F = new TreeSet<>(X_star);
        for(Vertex v : F){
            X_sub_F.remove(v);
        }
        return X_sub_F;
    }

    private Vertex init_find_min_degree(Set<Vertex> vs){
        Vertex min_v = null;
        int min_d = Integer.MAX_VALUE;
        for(Vertex v : vs){
            if(v.d < min_d){
                min_d = v.d;
                min_v = v;
            }
        }
        return min_v;
    }

    private Vertex init_find_max_degree(Set<Vertex> vs){
        Vertex max_v = null;
        int max_d = Integer.MIN_VALUE;
        for(Vertex v: vs){
            if(v.d > max_d){
                max_d = v.d;
                max_v = v;
            }
        }
        return max_v;
    }

    private void greedy_MCDS(){
        var F = new TreeSet<Vertex>();

        for(;;){
            Set<Vertex> X_sub_F = get_X_star_sub_F(F);
            if(X_sub_F.isEmpty())break;
            find_cut_vertices();
            Vertex u = init_find_min_degree(X_sub_F);
            if(u.is_cut){
                F.add(u);
            }else{
                X_star_remove(u);
                var u_neighbors = new TreeSet<Vertex>();
                for(DefaultEdge e : graph.edgesOf(u)){
                    Vertex v = get_the_other_edge_end(e,u);
                    u_neighbors.add(v);
                    if(X_star.contains(v)){
                        --v.d;
                    }
                }
                var tmp = new TreeSet<>(u_neighbors);
                tmp.retainAll(F);
                if(tmp.isEmpty()){
                    Vertex w = init_find_max_degree(u_neighbors);
                    F.add(w);
                }
            }
        }
    }

    private void initialization(){
        X_star = new TreeSet<>(graph.vertexSet());
        X_plus = new TreeSet<>();
        X_minu = new TreeSet<>();

        for (Vertex v : X_star){
            v.degree_to_X_star = graph.degreeOf(v);
            v.is_in_X_star = true;
            v.d = v.degree_to_X_star;
        }

        //greedy_MCDS();
        //System.out.println("Init done with size " + X_star.size());
    }


    private int depth;
    private int num_root_child;
    private Vertex root;

    private void find_cut_vertices () {
        depth = 1;
        num_root_child = 0;
        for(Vertex v : X_star){
            v.is_visited = false;
            v.dep = -1;
            v.low = -1;
            v.is_cut = false;
        }
        Vertex r = X_star.iterator().next();
        r.is_visited = true;
        root = r;
        cut_vertices_recur(r);
        if (num_root_child > 1){
            r.is_cut = true;
        }
    }

    private void cut_vertices_recur(Vertex r){
        r.is_visited = true;
        r.dep = depth;
        r.low = depth;
        ++depth;

        for (DefaultEdge e : graph.edgesOf(r)){
            Vertex tem = get_the_other_edge_end(e, r);
            if(tem.is_in_X_star){
                if(!tem.is_visited){
                    cut_vertices_recur(tem);
                    r.low = r.low < tem.low ? r.low : tem.low;
                    if (tem.low >= r.dep && r != root){
                        r.is_cut = true;
                    }else if (r == root){
                        ++num_root_child;
                    }
                }else{
                    r.low = r.low < tem.dep ? r.low : tem.dep;
                }
            }
        }
    }

    private void X_star_remove(Vertex v){
        X_star.remove(v);
        v.is_in_X_star = false;
        v.birth_iter = iter_count;

        if(X_star.isEmpty()){
            throw new Error("X_star cannot be empty");
        }
        X_plus.add(v);
        for (DefaultEdge e : graph.edgesOf(v)){
            Vertex u = get_the_other_edge_end(e, v);
            --u.degree_to_X_star;
            if (u.degree_to_X_star == 0 && !u.is_in_X_star){
                X_plus.remove(u);
                X_minu.add(u);
            }
        }
    }

    private void X_star_insert(Vertex v){
        X_plus.remove(v);
        X_star.add(v);
        v.is_in_X_star = true;
        v.birth_iter = iter_count;

        for (DefaultEdge e : graph.edgesOf(v)){
            Vertex u = get_the_other_edge_end(e, v);
            ++u.degree_to_X_star;
            if(u.degree_to_X_star == 1 && !X_star.contains(u)){
                X_minu.remove(u);
                X_plus.add(u);
            }
        }
    }

    private void shrink_X_star(){
        find_cut_vertices();
//        var non_cut_vertices = X_star.stream().filter(v -> !v.is_cut).collect(Collectors.toList());
//        X_star_remove(non_cut_vertices.get(random.nextInt(non_cut_vertices.size())));

        for (Vertex v : X_star){
            if(!v.is_cut){
                X_star_remove(v);
                return;
            }
        }

    }

    public boolean solve(int lower_bound){
        boolean is_hit = true;

        start_time = System.currentTimeMillis();
        initialization();

        while(X_star.size() > lower_bound){
            shrink_X_star();
            System.out.println("Solving " + X_star.size() + "-CDS...");
            if(X_minu.isEmpty())continue;

            local_search();

            if(X_minu.isEmpty()) {
                //check_solution();
                System.out.println("Successfully solved " + X_star.size() + "-CDS! iterations: " +
                        iter_count + ", time=" + (System.currentTimeMillis() - start_time) / 1000.0);
                time = (System.currentTimeMillis() - start_time) / 1000.0;
                min_X_star = new TreeSet<>(X_star);
                min_X_star_iter_count = iter_count;

            }else{
                is_hit = false;
                break;
            }
        }

        if(!is_hit) {
            roll_back(min_X_star);
        }
        check_solution();
        return is_hit;
    }


    public double getTime(){
        return time;
    }

    public Set<Vertex> getMin_X_star(){
        return min_X_star;
    }

    private void reset_ls(){
        ever_best_f = X_minu.size();
        f = ever_best_f;
        for(Vertex v : graph.vertexSet()){
            v.weight = 1;
            v.tabu_tenure = 0;
        }
    }

//    private void print_weight1(){
//        for(Vertex v : graph.vertexSet()){
//            if(v.weight == 1 && !v.is_cut){
//                System.out.print(v.index + " ");
//            }
//        }
//        System.out.println();
//    }

    private boolean local_search(){
        if(X_minu.isEmpty())return true;
        reset_ls();

        Set<Set<Vertex>> best_configs = new TreeSet<>(new Comp());
        best_configs.add(new TreeSet<>(X_star));
        long last_log_time = System.currentTimeMillis();
//        Set<Vertex> best_config = new TreeSet<>(X_star);
//        Comp cmp = new Comp();

        int fail_improve_count = 0;
        int ever_min_minu_size = X_minu.size();


        final int perturb_strength_base = (int)(X_star.size() * perturb_base_ratio);
        final int perturb_strength_max = X_star.size() - 1;
        int perturb_strength = perturb_strength_base;

        boolean is_descending = true;
        for(; !X_minu.isEmpty(); ++iter_count){
            if((System.currentTimeMillis() - start_time)/1000.0 > time_limit)break;
            Move mv = find_move();

            if(is_descending){
                if(mv.delta_f >= 0){
                    is_descending = false;
                }
            }else{
                if(mv.delta_f + f < ever_best_f){
                    is_descending = true;
                }
            }

            if(!is_descending){
                if (mv.delta_f >=0 && X_minu.size() < ever_min_minu_size) {
                    ever_min_minu_size = X_minu.size();
                    fail_improve_count = 0;
                    best_configs.clear();
                    best_configs.add(new TreeSet<>(X_star));
                    perturb_strength = perturb_strength_base;
                } else if (mv.delta_f >=0 && X_minu.size() == ever_min_minu_size) {
                    if (best_configs.contains(X_star)) {
                        perturb_strength = Math.min(perturb_strength + 1, perturb_strength_max);
                    } else {
                        perturb_strength = perturb_strength_base;
                        best_configs.add(new TreeSet<>(X_star));
                    }
                }else{
                    ++fail_improve_count;
                }
            }

            make_move(mv);

            if(f < ever_best_f){
                ever_best_f = f;
            }

            if(!is_descending){
                adjust_weight();
            }

            if (fail_improve_count > fail_improve_count_max) {
                fail_improve_count = 0;
                roll_back(get_random_in_set(best_configs));
                perturb_configuration(perturb_strength);
            }

            long curr_time = System.currentTimeMillis();
            if(curr_time - last_log_time > 10000) {
                last_log_time = curr_time;
//                print_weight1();
//                System.out.println(mv.insert_v + ", " + mv.remove_v);
                System.out.println("\t iter:" + iter_count + " X^- =" + X_minu.size() + ", ever_best=" + ever_min_minu_size
                + ", strength="+perturb_strength + ", config_count=" + best_configs.size());
            }

            //check_configuration();
        }

        //check_configuration();
        return !X_minu.isEmpty();
    }

    private void perturb_configuration(int perturb_strength){
        for(int i=0; i<perturb_strength; ++i){
            Move mv = get_random_move();
            make_move(mv);
        }
        for(Vertex v : graph.vertexSet()){
            v.weight = (int)Math.ceil(v.weight*0.1);
        }

        f = 0;
        for(Vertex v : X_minu){
            f += v.weight;
        }
        //check_configuration();
    }

    private Move get_random_move(){
        Vertex iv, rv;
        do{
            find_cut_vertices();
            iv = get_random_in_set(X_plus);

            var rv_candidate = X_star.stream().filter(v -> !v.is_cut).collect(Collectors.toList());
            rv = rv_candidate.get(random.nextInt(rv_candidate.size()));
        }while(!is_feasible_move(iv, rv));
        return new Move(iv, rv, calc_delta_f(iv, rv));
    }

    private boolean is_feasible_move(Vertex iv, Vertex rv){
        return !(graph.containsEdge(iv, rv) && iv.degree_to_X_star == 1);
    }

    private<T> T get_random_in_set(Set<T> set){
        int ri = random.nextInt(set.size());
        Iterator<T> iter = set.iterator();
        T e = null;
        for(int i=0; i<=ri; ++i){
            e = iter.next();
        }
        return e;
    }

    private void roll_back(Set<Vertex> bak_X_star){
        X_star.clear();
        X_plus.clear();
        X_minu.clear();
        X_minu.addAll(graph.vertexSet());
        for(Vertex v : graph.vertexSet()){
            v.degree_to_X_star = 0;
            v.is_in_X_star = false;
        }

        for (Vertex v : bak_X_star){
            X_minu.remove(v);
            X_plus.remove(v);
            X_star.add(v);
            v.is_in_X_star = true;
            for(DefaultEdge e : graph.edgesOf(v)){
                Vertex u = get_the_other_edge_end(e, v);
                if(!X_star.contains(u)){
                    X_minu.remove(u);
                    X_plus.add(u);
                }
                ++u.degree_to_X_star;
            }
        }
        find_cut_vertices();

        f = 0;
        for(Vertex v : X_minu){
            f += v.weight;
        }
    }

    private void adjust_weight(){
        for(Vertex v : X_minu){
            ++v.weight;
        }
        f += X_minu.size();
    }

    private List<Vertex> collect_vertices_to_insert(Vertex v){
        List<Vertex> i_vertices = new ArrayList<>();
        for(DefaultEdge e : graph.edgesOf(v)){
            Vertex u = get_the_other_edge_end(e, v);
            if(X_plus.contains(u)){
                i_vertices.add(u);
            }
        }
        return i_vertices;
    }

    private Vertex sole_connection_to_X_star(Vertex v){
        Vertex sole_connection = null;
        for(DefaultEdge e : graph.edgesOf(v)){
            Vertex u = get_the_other_edge_end(e, v);
            if(u.is_in_X_star) {
                if (sole_connection == null) {
                    sole_connection = u;
                }else{
                    return null;
                }
            }
        }
        return sole_connection;
    }

    private void make_move(Move mv){
        f += mv.delta_f;
        mv.remove_v.tabu_tenure = iter_count + random.nextInt(tabu_length) + base_tabu_length;
        X_star_insert(mv.insert_v);
        X_star_remove(mv.remove_v);
        //System.out.println("\tMove " + mv.insert_v + ", "+ mv.remove_v + ", delta:" + mv.delta_f + ", f="+f);
    }

    private Move find_move(){
        List<Vertex> i_v_list = prepare_iv_list();

        Move best_mv = new Move(null, null, Integer.MAX_VALUE);
        int best_count = 0;

        Move best_mv_tabu = new Move(null, null, Integer.MAX_VALUE);
        int best_count_tabu = 0;

        find_cut_vertices();

        for(Vertex i_v : i_v_list){
            Vertex exclude_v = X_star.size() > 1 ? sole_connection_to_X_star(i_v) : null;
            for(Vertex r_v : X_star){
                if(r_v.is_cut || r_v == exclude_v)continue;

                Move mv = new Move(i_v, r_v);

                if(iter_count > i_v.tabu_tenure) {
                    int cmp = compare_move_vanlila(mv, best_mv);
                    if (cmp < 0) {
                        if(mv.delta_X_minu_size < 0){
                            return mv;
                        }
                        best_mv = mv;
                        best_count = 1;
                    } else if (cmp == 0) {
                        if (random.nextInt(best_count + 1) == 0) {
                            best_mv = mv;
                        }
                        ++best_count;
                    }
                }else{
                    int cmp = compare_move_vanlila(mv, best_mv_tabu);
                    if (cmp < 0) {
                        best_mv_tabu = mv;
                        best_count_tabu = 1;
                    } else if (cmp == 0) {
                        if (random.nextInt(best_count_tabu + 1) == 0) {
                            best_mv_tabu = mv;
                        }
                        ++best_count_tabu;
                    }
                }
            }
        }

        if(best_mv.insert_v != null && best_mv_tabu.insert_v !=null){
            if(best_mv_tabu.delta_f < best_mv.delta_f
                    && best_mv_tabu.delta_f + f < ever_best_f){
                return best_mv_tabu;
            }else{
                return best_mv;
            }
        }else if(best_mv.insert_v == null && best_mv_tabu.insert_v !=null){
            return best_mv_tabu;
        }else if(best_mv.insert_v != null){
            return best_mv;
        }else{
            throw new Error("No move available!");
        }
    }

    private List<Vertex> prepare_iv_list(){
        var X_minu_list = new ArrayList<>(X_minu);
        Collections.shuffle(X_minu_list, random);
        List<Vertex> i_v_list=null;
        for (Vertex vertex : X_minu_list) {
            i_v_list = collect_vertices_to_insert(vertex);
            if (!i_v_list.isEmpty()) break;
        }

        return i_v_list;
    }

    private void compare_move_prepare(Move mv1, Move mv2){
        if(mv1.delta_f == Integer.MAX_VALUE && mv1.insert_v != null) {
            calc_delta(mv1);
//            mv1.delta_f = calc_delta_f(mv1.insert_v, mv1.remove_v);
        }
        if(mv2.delta_f == Integer.MAX_VALUE && mv2.insert_v != null){
            calc_delta(mv2);
//            mv2.delta_f = calc_delta_f(mv2.insert_v, mv2.remove_v);
        }
    }

    private int compare_move_vanlila(Move mv1, Move mv2){
        compare_move_prepare(mv1, mv2);
        return Integer.compare(mv1.delta_f, mv2.delta_f);
    }

    private void compare_move_prepare_weight(Move mv1, Move mv2){
        if(mv1.delta_risk_weight == Integer.MAX_VALUE){
            mv1.delta_risk_weight = calc_delta_risk_weight(mv1.insert_v, mv1.remove_v);
        }
        if(mv2.delta_risk_weight == Integer.MAX_VALUE){
            mv2.delta_risk_weight = calc_delta_risk_weight(mv2.insert_v, mv2.remove_v);
        }
    }

    private int compare_move_with_risk(Move mv1, Move mv2){
        compare_move_prepare(mv1, mv2);
        if(mv1.delta_f < mv2.delta_f){
            return -1;
        }else if (mv1.delta_f == mv2.delta_f){
            compare_move_prepare_weight(mv1, mv2);
            return Integer.compare(mv1.delta_risk_weight, mv2.delta_risk_weight);
        }else{
            return 1;
        }
    }

    private void compare_move_prepare_age(Move mv1, Move mv2){
        if(mv1.sum_age==Integer.MAX_VALUE){
            mv1.sum_age = iter_count - mv1.insert_v.birth_iter
                    + iter_count - mv1.remove_v.birth_iter;
        }
        if(mv2.sum_age==Integer.MAX_VALUE){
            mv2.sum_age = iter_count - mv2.insert_v.birth_iter
                    + iter_count - mv2.remove_v.birth_iter;
        }
    }

    private int compare_move_with_age(Move mv1, Move mv2){
        compare_move_prepare(mv1, mv2);
        if(mv1.delta_f < mv2.delta_f){
            return -1;
        }else if(mv1.delta_f == mv2.delta_f){
            compare_move_prepare_age(mv1, mv2);
            return Integer.compare(mv1.sum_age, mv2.sum_age);
        }else{
            return 1;
        }
    }

    private int compare_move(Move mv1, Move mv2){

        compare_move_prepare(mv1, mv2);

        if(mv1.delta_f < mv2.delta_f){
            return -1;
        }else if(mv1.delta_f == mv2.delta_f){
            compare_move_prepare_weight(mv1, mv2);
            if(mv1.delta_risk_weight < mv2.delta_risk_weight){
                return -1;
            }else if(mv1.delta_risk_weight == mv2.delta_risk_weight){
                compare_move_prepare_age(mv1, mv2);
                return Integer.compare(mv1.sum_age, mv2.sum_age);
            }else{
                return 1;
            }
        }else{
            return 1;
        }
    }

    private int calc_delta_risk_weight(Vertex i_v, Vertex r_v){
        int delta_risk_weight = 0;
        for(DefaultEdge e: graph.edgesOf(i_v)){
            Vertex u = get_the_other_edge_end(e, i_v);
            if(u.degree_to_X_star == 0){
                delta_risk_weight += u.weight;
            }else if(u.degree_to_X_star == 1){
                delta_risk_weight -= u.weight;
            }
        }
        for(DefaultEdge e : graph.edgesOf(r_v)){
            Vertex u = get_the_other_edge_end(e, r_v);
            if(u.degree_to_X_star == 1 && !graph.containsEdge(u, i_v)){
                delta_risk_weight -= u.weight;
            }else if(u.degree_to_X_star == 2 && !graph.containsEdge(u, i_v)){
                delta_risk_weight += u.weight;
            }
        }

        return delta_risk_weight;
    }

    private void calc_delta(Move mv){
        mv.delta_f = 0;
        mv.delta_X_minu_size = 0;
        for(DefaultEdge e : graph.edgesOf(mv.insert_v)){
            Vertex u = get_the_other_edge_end(e, mv.insert_v);
            if(u.degree_to_X_star == 0){
                --mv.delta_X_minu_size;
                mv.delta_f -= u.weight;
            }
        }
        for(DefaultEdge e : graph.edgesOf(mv.remove_v)){
            Vertex u = get_the_other_edge_end(e, mv.remove_v);
            if(u.degree_to_X_star == 1 && !graph.containsEdge(u, mv.insert_v)){
                ++mv.delta_X_minu_size;
                mv.delta_f += u.weight;
            }
        }
    }

    private int calc_delta_f(Vertex i_v, Vertex r_v){
        int delta_f = 0;
        for(DefaultEdge e: graph.edgesOf(i_v)){
            Vertex u = get_the_other_edge_end(e, i_v);
            if(u.degree_to_X_star == 0){
                //--delta_f;
                delta_f -= u.weight;
            }
        }

        for(DefaultEdge e : graph.edgesOf(r_v)){
            Vertex u = get_the_other_edge_end(e, r_v);
            if(u.degree_to_X_star == 1 && !graph.containsEdge(u, i_v)){
                //++delta_f;
                delta_f += u.weight;
            }
        }
        return delta_f;
    }



    private Vertex get_the_other_edge_end(DefaultEdge e, Vertex v){
        Vertex u = graph.getEdgeSource(e);
        if (u==v){
            u = graph.getEdgeTarget(e);
        }
        return u;
    }

    private void check_solution(){
        if(!X_minu.isEmpty()){
            throw new Error("Not a feasible solution! 1");
        }
        check_set_split();
        check_connectivity();
        for(Vertex v : graph.vertexSet()){
            if(!X_star.contains(v)){
                boolean is_dominated = false;
                for (DefaultEdge e : graph.edgesOf(v)){
                    Vertex u = get_the_other_edge_end(e,v);
                    if(X_star.contains(u)){
                        is_dominated = true;
                        break;
                    }
                }
                if(!is_dominated){
                    throw new Error("Not a feasible solution! 2");
                }
            }
        }
    }

    private void check_configuration(){
        check_set_split();
        check_f();
        check_consistency();
        check_connectivity();
    }

    private void check_f(){
        int calc_f = 0;
        for(Vertex v : X_minu){
            calc_f += v.weight;
        }

        if(calc_f != f){
            throw new Error("check_f");
        }
    }

    private void dfs(Vertex r){
        r.is_visited = true;
        for (DefaultEdge e : graph.edgesOf(r)){
            Vertex u = get_the_other_edge_end(e, r);
            if(!u.is_visited && X_star.contains(u)){
                dfs(u);
            }
        }
    }

    private void check_connectivity(){
        for(Vertex v : X_star){
            v.is_visited = false;
        }

        Vertex v = X_star.iterator().next();
        dfs(v);
        for(Vertex u : X_star){
            if(!u.is_visited){
                throw new Error("X^* is not connected!");
            }
        }
    }

    private void check_consistency(){
        for (Vertex v: graph.vertexSet()){
            if (X_star.contains(v)){
                if(!v.is_in_X_star){
                    throw new Error("check_consistency 1");
                }
            }else if(X_plus.contains(v)){
                boolean is_dominated = false;
                for(DefaultEdge e : graph.edgesOf(v)){
                    Vertex u = get_the_other_edge_end(e, v);
                    if(X_star.contains(u)){
                        is_dominated = true;
                        break;
                    }
                }
                if(!is_dominated){
                    throw new Error("check_consistency 2");
                }
            }else if(X_minu.contains(v)){
                for(DefaultEdge e : graph.edgesOf(v)){
                    Vertex u = get_the_other_edge_end(e, v);
                    if(X_star.contains(u)){
                        throw new Error("check_consistency 3");
                    }
                }
            }else{
                throw new Error("check_consistency 4");
            }

            int d_x = 0;
            for(DefaultEdge e : graph.edgesOf(v)){
                Vertex u = get_the_other_edge_end(e, v);
                if(X_star.contains(u)){
                    ++d_x;
                }
            }
            if(d_x != v.degree_to_X_star){
                throw new Error("check_consistency 5");
            }
        }
    }

    private void check_set_split(){
        Set<Vertex> tmp = new TreeSet<>(X_star);
        tmp.retainAll(X_plus);
        if(!tmp.isEmpty()){
            throw new Error("X^* and X^+ share elements");
        }

        tmp = new TreeSet<>(X_star);
        tmp.retainAll(X_minu);
        if(!tmp.isEmpty()){
            throw new Error("X^* and X^- share elements");
        }

        tmp = new TreeSet<>(X_plus);
        tmp.retainAll(X_minu);
        if(!tmp.isEmpty()){
            throw new Error("X^+ and X^- share elements");
        }
    }

    public int getIter_count(){
        return min_X_star_iter_count;
    }
}
