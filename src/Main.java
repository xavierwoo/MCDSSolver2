import mcdsSolver.MCDSSolver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static List<Instance> LMS(){
        List<Instance> instances = new ArrayList<>();
        instances.add(new Instance("instances/MLSTP/v100_d5.dat",24));//first one is the warm up
        instances.add(new Instance("instances/MLSTP/v100_d5.dat",24));
        instances.add(new Instance("instances/MLSTP/v100_d10.dat",13));
        instances.add(new Instance("instances/MLSTP/v100_d20.dat",8));
        instances.add(new Instance("instances/MLSTP/v100_d30.dat",6));
        instances.add(new Instance("instances/MLSTP/v100_d50.dat",4));
        instances.add(new Instance("instances/MLSTP/v100_d70.dat",3));
        instances.add(new Instance("instances/MLSTP/v120_d5.dat",25));
        instances.add(new Instance("instances/MLSTP/v120_d10.dat",13));
        instances.add(new Instance("instances/MLSTP/v120_d20.dat",8));
        instances.add(new Instance("instances/MLSTP/v120_d30.dat",6));
        instances.add(new Instance("instances/MLSTP/v120_d50.dat",4));
        instances.add(new Instance("instances/MLSTP/v120_d70.dat",3));
        instances.add(new Instance("instances/MLSTP/v150_d5.dat",26));
        instances.add(new Instance("instances/MLSTP/v150_d10.dat",14));
        instances.add(new Instance("instances/MLSTP/v150_d20.dat",9));
        instances.add(new Instance("instances/MLSTP/v150_d30.dat",6));
        instances.add(new Instance("instances/MLSTP/v150_d50.dat",4));
        instances.add(new Instance("instances/MLSTP/v150_d70.dat",3));
        instances.add(new Instance("instances/MLSTP/v200_d5.dat",27));
        instances.add(new Instance("instances/MLSTP/v200_d10.dat",16));
        instances.add(new Instance("instances/MLSTP/v200_d20.dat",9));
        instances.add(new Instance("instances/MLSTP/v200_d30.dat",7));
        instances.add(new Instance("instances/MLSTP/v200_d50.dat",4));
        instances.add(new Instance("instances/MLSTP/v200_d70.dat",3));
        return instances;
    }

    public static List<Instance> calibration_instances(){
        List<Instance> instances = new ArrayList<>();
        instances.add(new Instance("instances/MLSTP/v200_d5.dat",27));
        instances.add(new Instance("instances/IEEE/ieee_300_bus.txt",129));
        instances.add(new Instance("instances/MCDSP/n1000_200_r100.txt",31));
        instances.add(new Instance("instances/MCDSP/n2000_300_r200.txt",33));
        instances.add(new Instance("instances/MCDSP/n3000_400_r210",61));

        return instances;
    }

    public static void main(String[] args) throws IOException {

        List<Instance> instances = calibration_instances();

        BufferedWriter bw = new BufferedWriter(new FileWriter("res.csv", true));
        bw.write("instance,best,time,avg\n");
        bw.close();

        for(Instance ins : instances){
            String[] tmp = ins.file_name.split("/");
            String ins_name = tmp[tmp.length-1];

            double best_time = Double.MAX_VALUE;
            int best_min_size = Integer.MAX_VALUE;
            var res_list = new ArrayList<Integer>();

            for(int run=0; run<10; ++run) {
                MCDSSolver solver = new MCDSSolver(ins.file_name);
                solver.solve(ins.lb);

                double time = solver.getTime();
                int min_size = solver.getMin_X_star().size();

                res_list.add(min_size);
                if(min_size < best_min_size){
                    best_min_size = min_size;
                    best_time = time;
                }else if(min_size == best_min_size && time < best_time){
                    best_time = time;
                }
            }

            double avg = res_list.stream().mapToDouble(a->a).average().orElse(Double.NaN);

            bw = new BufferedWriter(new FileWriter("res.csv", true));

            bw.write(ins_name + "," + best_min_size + "," + best_time + "," + avg + "\n");

            bw.close();
        }
    }
}
