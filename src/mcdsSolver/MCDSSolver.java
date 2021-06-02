package mcdsSolver;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MCDSSolver {

    Random random = new Random();

    private Set<Vertex> X_star;
    private Set<Vertex> X_plus;
    private Set<Vertex> X_minu;

    private Set<Vertex> min_X_star = null;
    private int min_X_star_iter_count = 0;

    private final Graph graph = new Graph();

    private int iter_count = 0;
    private int f;
    private int ever_best_f;

    private int tabu_length = 10;
    private int base_tabu_length = 1;
    private double perturb_base_ratio = 0.1;
    private int fail_improve_count_max = 320000;
    private double time_limit = 600;

    private long start_time;

    private double time = 0;

    public MCDSSolver(String instance, int seed) throws IOException {
        random = new Random(seed);
        readInstance(instance);
    }

    public void setTabu_length(int  tl){
        tabu_length = tl;
    }

    public void setBase_tabu_length(int btl){
        base_tabu_length = btl;
    }

    public void setPerturb_base_ratio(double r){
        perturb_base_ratio = r;
    }

    public void setFail_improve_count_max(int c){
        fail_improve_count_max = c;
    }

    public void setTime_limit(int s){
        time_limit = s;
    }

    public void readInstance(String instance) throws IOException {
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

            graph.addEdge(source, target);
        }
        graph.render_matrix();
        System.out.println("Instance loaded, using time " + (System.currentTimeMillis() - s_time)/1000.0);
    }

    public MCDSSolver(String instance) throws IOException {
        readInstance(instance);
    }

    private Vertex get_vertex(int vIndex){
        for(Vertex v : graph.vertexSet()){
            if(v.index == vIndex){
                return v;
            }
        }
        Vertex v = new Vertex(vIndex);
        graph.addVertex(v);
        return v;
    }

    private void initialization(){
        X_star = new TreeSet<>(graph.vertexSet());
        X_plus = new TreeSet<>();
        X_minu = new TreeSet<>();

        for (Vertex v : X_star){
            v.degree_to_X_star = graph.degreeOf(v);
            v.is_in_X_star = true;
        }
    }

//    private void fill_array(int[] array, int value){
//        int len = array.length;
//        if (len > 0){
//            array[0] = value;
//        }
//        for (int i = 1; i < len; i += i) {
//            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
//        }
//    }

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
            v.is_cut_previously = v.is_cut;
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

        for(Vertex tem : graph.neighborsOf(r)){
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

        for(Vertex u : graph.neighborsOf(v)){
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

        for (Vertex u : graph.neighborsOf(v)){
            ++u.degree_to_X_star;
            if(!u.is_in_X_star && u.degree_to_X_star > 0) {
                if (u.degree_to_X_star == 1) {
                    X_minu.remove(u);
                    X_plus.add(u);
                }
            }
        }
    }

    private void shrink_X_star(){
        find_cut_vertices();

        Vertex best_v = null;
        int best_delta_f = Integer.MAX_VALUE;
        int best_count = 0;
        for(Vertex v : X_star){
            if(v.is_cut)continue;
            int delta_f = 0;
            for(Vertex u: graph.neighborsOf(v)){
                if(u.degree_to_X_star == 1){
                    delta_f += u.weight;
                }
            }
            if(delta_f < best_delta_f){
                best_delta_f = delta_f;
                best_v = v;
                best_count = 1;
            }else if(delta_f == best_delta_f){
                ++best_count;
                if(random.nextInt(best_count) == 0){
                    best_v = v;
                }
            }
        }
        X_star_remove(best_v);
    }

    private void grow_set(int k){
        var vertexSet = graph.vertexSet();
        X_star.clear();
        X_plus.clear();
        X_minu.clear();
        X_minu.addAll(vertexSet);
        for(Vertex v : vertexSet){
            v.degree_to_X_star = 0;
            v.is_in_X_star = false;
        }

        Vertex seed = vertexSet.get(random.nextInt(vertexSet.size()));
        X_minu.remove(seed);

        X_star_insert(seed);

        while(X_star.size() < k){
            Vertex v = get_random_in_set(X_plus);
            X_star_insert(v);
        }
    }

    private void sample () throws IOException {
        int high = X_star.size();
        int low = 2;
        min_X_star = new TreeSet<>(X_star);

        while(low < high){
            int k = (high + low) / 2;
            grow_set(k);
            find_cut_vertices();
            System.out.println("Trying " + X_star.size() + "-CDS...");
            if(X_minu.isEmpty() || local_search(true)){
                check_solution();
                System.out.println("\t Succeed!");
                min_X_star = new TreeSet<>(X_star);
                high = k - 1;
            }else{
                low = k + 1;
                System.out.println("\t Fail.");
                roll_back(min_X_star);
            }
        }
        check_configuration();
    }

    public boolean solve (int lower_bound) throws IOException {
        boolean is_hit = true;

        start_time = System.currentTimeMillis();
        initialization();

        //sample();

        while(X_star.size() > lower_bound){

            shrink_X_star();

            System.out.println("Solving " + X_star.size() + "-CDS...");
            if(X_minu.isEmpty()){
                check_solution();
                min_X_star = new TreeSet<>(X_star);
                min_X_star_iter_count = iter_count;
                //write objective and time to file
//                BufferedWriter bf = new BufferedWriter(new FileWriter("objTime.txt", true));
//                bf.write(X_star.size() + "\t" + time + "\n");
//                bf.close();
                continue;
            }

            local_search(false);

            if(X_minu.isEmpty()) {
                check_solution();
                System.out.println("Successfully solved " + X_star.size() + "-CDS! iterations: " +
                        iter_count + ", time=" + (System.currentTimeMillis() - start_time) / 1000.0);
                time = (System.currentTimeMillis() - start_time) / 1000.0;
                min_X_star = new TreeSet<>(X_star);
                min_X_star_iter_count = iter_count;

                //write objective and time to file
//                BufferedWriter bf = new BufferedWriter(new FileWriter("objTime.txt", true));
//                bf.write(X_star.size() + "\t" + time + "\n");
//                bf.close();

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

    private void calc_insert_delta(Vertex v){
        v.insert_delta_f = 0;
        v.insert_delta_size = 0;

        for(Vertex u : graph.neighborsOf(v)){
            if(u.degree_to_X_star == 0){
                --v.insert_delta_size;
                v.insert_delta_f -= u.weight;
            }
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

    private void check_X_plus_delta(){
        for(Vertex v : X_plus){
            int delta_f = 0;
            int delta_size = 0;
            for(Vertex u : graph.neighborsOf(v)){
                if(u.degree_to_X_star == 0){
                    --delta_size;
                    delta_f -= u.weight;
                }
            }

            if(delta_f != v.insert_delta_f || delta_size != v.insert_delta_size){
                throw new Error("check_X_plus_delta");
            }
        }
    }

    private void write_iters() throws IOException {
        BufferedWriter bf = new BufferedWriter(new FileWriter("iter.txt", true));

        bf.write((System.currentTimeMillis() - start_time)/1000.0 + "\t" + iter_count + "\n");

        bf.close();
    }

    private boolean local_search(boolean is_descent_only) throws IOException {
        if(X_minu.isEmpty())return true;
        reset_ls();
        //init_delta_values();

        Map<Set<Vertex>, Integer> best_configs = new TreeMap<>(new Comp());
        best_configs.put(Collections.unmodifiableSet(new TreeSet<>(X_star)), 1);
        long last_log_time = System.currentTimeMillis();
//        Set<Vertex> best_config = new TreeSet<>(X_star);
//        Comp cmp = new Comp();

        int fail_improve_count = 0;
        int ever_min_minu_size = X_minu.size();


        final int perturb_strength_base = Math.max(3,(int)(X_star.size() * perturb_base_ratio));
//        final int perturb_strength_max = X_star.size() - 1;
        final int perturb_strength_max = X_star.size() /3;
        int perturb_strength = perturb_strength_base;

        boolean is_descending = true;
        int phase = 1;

        find_cut_vertices();
        for(; !X_minu.isEmpty() ; ++iter_count){

            if((System.currentTimeMillis() - start_time)/1000.0 > time_limit)break;

            Move mv;
            mv = find_move(phase);


            if(is_descending){
                if(mv.delta_f >= 0){
                    is_descending = false;
                }
            }else{
                if(mv.delta_f + f < ever_best_f){
                    is_descending = true;
                    phase = 1;
                }
            }

            if(is_descent_only && !is_descending)break;

            if(!is_descending){
                if (mv.delta_f >=0 && X_minu.size() < ever_min_minu_size) {
                    ever_min_minu_size = X_minu.size();
                    fail_improve_count = 0;
                    best_configs.clear();
                    best_configs.put(new TreeSet<>(X_star), 1);
                    perturb_strength = perturb_strength_base;
                    phase = 1;
                } else if (mv.delta_f >=0 && X_minu.size() == ever_min_minu_size) {
                    Integer count = best_configs.get(X_star);
                    if(count != null){
                        perturb_strength = Math.min(perturb_strength + 1, perturb_strength_max);
                        best_configs.put(X_star, count + 1);
                    }else if(best_configs.size() < 50000){
                        perturb_strength = perturb_strength_base;
                        best_configs.put(Collections.unmodifiableSet(new TreeSet<>(X_star)), 1);
                        phase = 1;
                    }else{
                        ++fail_improve_count;
                    }
                }else{
                    ++fail_improve_count;
                }
            }

            make_move(mv);

            if(f < ever_best_f){
                ever_best_f = f;
            }

            if (fail_improve_count > fail_improve_count_max) {
                fail_improve_count = 0;
                Set<Vertex> best_bak = get_best_bak(best_configs);
                roll_back(best_bak);
                perturb_configuration(perturb_strength);
                ++phase;

                continue;
            }

            if(!is_descending){
                adjust_weight();
            }

            long curr_time = System.currentTimeMillis();
            if(curr_time - last_log_time > 10000) {
                last_log_time = curr_time;
//                print_weight1();
//                System.out.println(mv.insert_v + ", " + mv.remove_v);
                System.out.println("\t iter:" + iter_count + " X^- =" + X_minu.size() + ", ever_best=" + ever_min_minu_size
                        + ", strength="+perturb_strength + ", config_count=" + best_configs.size()
                + " |DEBUG-> phase=" + phase);
            }
        }

        //check_configuration();
        return X_minu.isEmpty();
    }

    private Set<Vertex> get_best_bak(Map<Set<Vertex>, Integer> baks){
        Set<Vertex> best_bak = null;
        int min = Integer.MAX_VALUE;
        int best_count = 0;
        for(Map.Entry<Set<Vertex>, Integer> entry : baks.entrySet()){
            int bak_value = entry.getValue();
            if(bak_value < min){
                min = bak_value;
                best_count = 1;
                best_bak = entry.getKey();
            }else if(bak_value == min){
                ++best_count;
                if(random.nextInt(best_count) == 0){
                    best_bak = entry.getKey();
                }
            }
        }
        //System.out.println("best bak value:" + min);
        return best_bak;
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

        find_cut_vertices();
        //init_delta_values();

        check_configuration();
    }

    private Move get_random_move(){
        Vertex iv, rv;
        do{
            find_cut_vertices();
            iv = get_random_in_set(X_plus);

            var rv_candidate = X_star.stream().filter(v -> !v.is_cut).collect(Collectors.toList());
            rv = rv_candidate.get(random.nextInt(rv_candidate.size()));
        }while(!is_feasible_move(iv, rv));

        Move mv = new Move(iv, rv);
        calc_delta(mv, Integer.MAX_VALUE);
        return mv;
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
            for(Vertex u : graph.neighborsOf(v)){
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

    private List<Vertex> collect_vertices_to_insert(Vertex v, int phase){

        Set<Vertex> con_Xm = new TreeSet<>();
        int cutoff = 10;
//        TODO: restore here
//        int cutoff = Integer.MAX_VALUE;

        for(Vertex u : graph.neighborsOf(v)){
            if(!u.is_in_X_star && u.degree_to_X_star > 0){
                con_Xm.add(u);
            }
        }


        List<Vertex> res = new ArrayList<>(con_Xm);


        for(Vertex u : res){
            calc_insert_delta(u);
        }

        Collections.sort(res, (a, b)->{
            if(a.insert_delta_f < b.insert_delta_f){
                return -1;
            }else if(a.insert_delta_f == b.insert_delta_f){
                return Integer.compare(a.birth_iter, b.birth_iter);
            }else{
                return 1;
            }
        });


        if(res.size() > cutoff){
            res = res.subList(0, cutoff);
        }

        return res;
    }

    private Vertex sole_connection_to_X_star(Vertex v){
        Vertex sole_connection = null;
        for(Vertex u : graph.neighborsOf(v)){
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
        find_cut_vertices();
        //System.out.println("\tMove " + mv.insert_v + ", "+ mv.remove_v + ", delta:" + mv.insert_delta_f + ", f="+f);
    }

    private Move find_move(int phase){
        List<Vertex> i_v_list = prepare_iv_list(phase);

        Move best_mv = new Move(null, null, Integer.MAX_VALUE);
        int best_count = 0;

        Move best_mv_tabu = new Move(null, null, Integer.MAX_VALUE);
        int best_count_tabu = 0;

        boolean found_best_improve = false;
        int fail_best_improve_count = 0;

        for(Vertex i_v : i_v_list){
            var candidate_remove_v =
                    X_star.stream().filter(v-> !v.is_cut)
                            .collect(Collectors.toList());
            Collections.shuffle(candidate_remove_v, random);


            if(i_v.tabu_tenure < iter_count) {
                if (i_v.insert_delta_f > best_mv.delta_f) break;
            }else{
                if (i_v.insert_delta_f > best_mv_tabu.delta_f) break;
            }


            for(Vertex r_v : candidate_remove_v){
                if(i_v.degree_to_X_star == 1 && graph.containsEdge(i_v, r_v))continue;
                Move mv = new Move(i_v, r_v);

                if(iter_count > i_v.tabu_tenure) {
                    int cmp = compare_move(mv, best_mv);
                    if (cmp < 0) {

                        if(mv.delta_X_minu_size < 0){
                            found_best_improve = true;
                            fail_best_improve_count = 0;
                        }
                        best_mv = mv;
                        best_count = 1;
                    } else if (cmp == 0) {
                        if (random.nextInt(best_count + 1) == 0) {
                            best_mv = mv;
                        }
                        ++best_count;
                        if(found_best_improve) fail_best_improve_count++;
                    } else {
                        if(found_best_improve) fail_best_improve_count++;
                    }
                    if(fail_best_improve_count > 0){
                        return best_mv;
                    }
                }else{
                    int cmp = compare_move(mv, best_mv_tabu);
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

    private List<Vertex> prepare_iv_list(int phase){
        var X_minu_list = new ArrayList<>(X_minu);
        Collections.shuffle(X_minu_list, random);
        List<Vertex> i_v_list=null;
        for (Vertex vertex : X_minu_list) {
            i_v_list = collect_vertices_to_insert(vertex, phase);
            if (!i_v_list.isEmpty()) break;
        }

        return i_v_list;
    }

    private List<Vertex> getAllConXm(){
        var i_v_set = new TreeSet<Vertex>();

        for(Vertex v : X_minu){
            for(Vertex u : graph.neighborsOf(v)){
                if(X_plus.contains(u)){
                    i_v_set.add(u);
                }
            }
        }

        return new ArrayList<>(i_v_set);
    }

    private void compare_move_prepare(Move mv1, Move best_mv){
        if(mv1.delta_f == Integer.MAX_VALUE && mv1.insert_v != null) {
            calc_delta(mv1, best_mv.delta_f);
        }
    }

    private int compare_move(Move mv1, Move best_mv){
        compare_move_prepare(mv1, best_mv);
        return Integer.compare(mv1.delta_f, best_mv.delta_f);
    }

    private void calc_delta(Move mv, int cutoff){

        mv.delta_f = mv.insert_v.insert_delta_f;
        mv.delta_X_minu_size = mv.insert_v.insert_delta_size;

        if(mv.delta_f > cutoff)return;

        for(Vertex u : graph.neighborsOf(mv.remove_v)){
            if(u.degree_to_X_star == 1 && !graph.containsEdge(u, mv.insert_v)){
                ++mv.delta_X_minu_size;
                mv.delta_f += u.weight;
                if(mv.delta_f > cutoff)return;
            }
        }
    }

    private int calc_delta_f(Vertex i_v, Vertex r_v){
        int delta_f = 0;
        for(Vertex u: graph.neighborsOf(i_v)){
            if(u.degree_to_X_star == 0){
                delta_f -= u.weight;
            }
        }

        for(Vertex u : graph.neighborsOf(r_v)){
            if(u.degree_to_X_star == 1 && !graph.containsEdge(u, i_v)){
                delta_f += u.weight;
            }
        }
        return delta_f;
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
                for(Vertex u : graph.neighborsOf(v)){
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

    private void dfs_Xs(Vertex r){
        r.is_visited = true;
        for(Vertex u : graph.neighborsOf(r)){
            if(r.is_in_X_star && !u.is_visited){
                dfs_Xs(u);
            }
        }
    }

    private void check_connectivity(){
        for(Vertex v : X_star){
            v.is_visited = false;
        }

        Vertex v = X_star.iterator().next();
        dfs_Xs(v);
        for(Vertex u : X_star){
            if(!u.is_visited){
                throw new Error("X^* is not connected! " + iter_count);
            }
        }
    }

    private void printGX(Set<Vertex> X) throws java.io.IOException{

        BufferedWriter bf = new BufferedWriter(new FileWriter("debug.txt"));

        for(Vertex u : X){
            for(Vertex v : graph.neighborsOf(u)){
                if(v.is_in_X_star && v.index > u.index){
                    bf.write(u.index + "--" + v.index + "\n");
                }
            }
        }

        bf.close();
    }

    private void check_consistency(){
        for (Vertex v: graph.vertexSet()){
            if (X_star.contains(v)){
                if(!v.is_in_X_star){
                    throw new Error("check_consistency 1");
                }
            }else if(X_plus.contains(v)){
                boolean is_dominated = false;
                for(Vertex u : graph.neighborsOf(v)){
                    if(X_star.contains(u)){
                        is_dominated = true;
                        break;
                    }
                }
                if(!is_dominated){
                    throw new Error("check_consistency 2");
                }
            }else if(X_minu.contains(v)){
                for(Vertex u : graph.neighborsOf(v)){
                    if(X_star.contains(u)){
                        throw new Error("check_consistency 3");
                    }
                }
            }else{
                throw new Error("check_consistency 4");
            }

            int d_x = 0;
            for(Vertex u : graph.neighborsOf(v)){
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
