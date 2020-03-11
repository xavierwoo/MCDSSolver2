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

    public static List<Instance> IEEE(){
        List<Instance> instances = new ArrayList<>();
        instances.add(new Instance("instances/IEEE/ieee_14_bus.txt",5));
        instances.add(new Instance("instances/IEEE/ieee_14_bus.txt",5));
        instances.add(new Instance("instances/IEEE/ieee_30_bus.txt",11));
        instances.add(new Instance("instances/IEEE/ieee_57_bus.txt",31));
        instances.add(new Instance("instances/IEEE/rts96.txt",32));
        instances.add(new Instance("instances/IEEE/ieee_118_bus.txt",43));
        instances.add(new Instance("instances/IEEE/ieee_300_bus.txt",129));
        return instances;
    }

    public static List<Instance> calibration_instances(){
        List<Instance> instances = new ArrayList<>();
//        instances.add(new Instance("instances/MLSTP/v200_d5.dat",34));
//        instances.add(new Instance("instances/MLSTP/v200_d5.dat",27));
        instances.add(new Instance("instances/IEEE/ieee_300_bus.txt",129));
        instances.add(new Instance("instances/MCDSP/n1000_200_r100.txt",38));
        instances.add(new Instance("instances/MCDSP/n1500_250_r130.txt",49));
        instances.add(new Instance("instances/MCDSP/n2000_300_r200.txt",41));
        instances.add(new Instance("instances/MCDSP/n2500_350_r200.txt",60));
        instances.add(new Instance("instances/MCDSP/n2500_350_r210.txt",54));
        instances.add(new Instance("instances/MCDSP/n2500_350_r220.txt",51));
        instances.add(new Instance("instances/MCDSP/n2500_350_r230.txt",48));
        instances.add(new Instance("instances/MCDSP/n3000_400_r210.txt",74));
        instances.add(new Instance("instances/MCDSP/n3000_400_r220.txt",69));
        instances.add(new Instance("instances/MCDSP/n3000_400_r230.txt",64));
        instances.add(new Instance("instances/MCDSP/n3000_400_r240.txt",60));

        return instances;
    }

    public static List<Instance> BOBL(){
        List<Instance> instances = new ArrayList<>();
//        instances.add(new Instance("instances/BOBL/n1000_ep0007.rg",1));
//        instances.add(new Instance("instances/BOBL/n1000_ep0014.rg",1));
//        instances.add(new Instance("instances/BOBL/n1000_ep0028.rg",1));
//        instances.add(new Instance("instances/BOBL/n1000_ep0056.rg",1));
//        instances.add(new Instance("instances/BOBL/n1000_ep0112.rg",1));
//        instances.add(new Instance("instances/BOBL/n1000_ep0224.rg",1));

//        instances.add(new Instance("instances/BOBL/n1000_r0048.rgg",1));
//        instances.add(new Instance("instances/BOBL/n1000_r0070.rgg",1));
//        instances.add(new Instance("instances/BOBL/n1000_r0100.rgg",1));
//        instances.add(new Instance("instances/BOBL/n1000_r0140.rgg",1));
//        instances.add(new Instance("instances/BOBL/n1000_r0207.rgg",1));
//        instances.add(new Instance("instances/BOBL/n1000_r0308.rgg",1));

//        instances.add(new Instance("instances/BOBL/n5000_ep0007.rg",1));
//        instances.add(new Instance("instances/BOBL/n5000_ep0014.rg",1));
//        instances.add(new Instance("instances/BOBL/n5000_ep0028.rg",1));
//        instances.add(new Instance("instances/BOBL/n5000_ep0056.rg",1));
//        instances.add(new Instance("instances/BOBL/n5000_ep0112.rg",1));
//        instances.add(new Instance("instances/BOBL/n5000_ep0224.rg",1));
//
//        instances.add(new Instance("instances/BOBL/n5000_r0048.rgg",1));
//        instances.add(new Instance("instances/BOBL/n5000_r0070.rgg",1));
//        instances.add(new Instance("instances/BOBL/n5000_r0100.rgg",1));
//        instances.add(new Instance("instances/BOBL/n5000_r0140.rgg",1));
//        instances.add(new Instance("instances/BOBL/n5000_r0207.rgg",1));
        instances.add(new Instance("instances/BOBL/n5000_r0308.rgg",1));

        return instances;
    }

    public static List<Instance> MCDSP(){
        List<Instance> instances = new ArrayList<>();
        instances.add(new Instance("instances/MCDSP/n400_80_r60.txt",19));
        instances.add(new Instance("instances/MCDSP/n400_80_r60.txt",17));
        instances.add(new Instance("instances/MCDSP/n400_80_r70.txt",13));
        instances.add(new Instance("instances/MCDSP/n400_80_r80.txt",11));
        instances.add(new Instance("instances/MCDSP/n400_80_r90.txt",9));
        instances.add(new Instance("instances/MCDSP/n400_80_r100.txt",7));
        instances.add(new Instance("instances/MCDSP/n400_80_r110.txt",6));
        instances.add(new Instance("instances/MCDSP/n400_80_r120.txt",5));

        instances.add(new Instance("instances/MCDSP/n600_100_r80.txt",20));
        instances.add(new Instance("instances/MCDSP/n600_100_r90.txt",18));
        instances.add(new Instance("instances/MCDSP/n600_100_r100.txt",15));
        instances.add(new Instance("instances/MCDSP/n600_100_r110.txt",14));
        instances.add(new Instance("instances/MCDSP/n600_100_r120.txt",12));

        instances.add(new Instance("instances/MCDSP/n700_200_r70.txt",38));
        instances.add(new Instance("instances/MCDSP/n700_200_r80.txt",31));
        instances.add(new Instance("instances/MCDSP/n700_200_r90.txt",20));
        instances.add(new Instance("instances/MCDSP/n700_200_r100.txt",17));
        instances.add(new Instance("instances/MCDSP/n700_200_r110.txt",15));
        instances.add(new Instance("instances/MCDSP/n700_200_r120.txt",12));
//
        instances.add(new Instance("instances/MCDSP/n1000_200_r100.txt",37));
        instances.add(new Instance("instances/MCDSP/n1000_200_r110.txt",33));
        instances.add(new Instance("instances/MCDSP/n1000_200_r120.txt",28));
        instances.add(new Instance("instances/MCDSP/n1000_200_r130.txt",25));
        instances.add(new Instance("instances/MCDSP/n1000_200_r140.txt",22));
        instances.add(new Instance("instances/MCDSP/n1000_200_r150.txt",20));
        instances.add(new Instance("instances/MCDSP/n1000_200_r160.txt",18));

        instances.add(new Instance("instances/MCDSP/n1500_250_r130.txt",48));
        instances.add(new Instance("instances/MCDSP/n1500_250_r140.txt",42));
        instances.add(new Instance("instances/MCDSP/n1500_250_r150.txt",39));
        instances.add(new Instance("instances/MCDSP/n1500_250_r160.txt",35));
//
        instances.add(new Instance("instances/MCDSP/n2000_300_r200.txt",40));
        instances.add(new Instance("instances/MCDSP/n2000_300_r210.txt",37));
        instances.add(new Instance("instances/MCDSP/n2000_300_r220.txt",34));
        instances.add(new Instance("instances/MCDSP/n2000_300_r230.txt",32));

        instances.add(new Instance("instances/MCDSP/n2500_350_r200.txt",59));
        instances.add(new Instance("instances/MCDSP/n2500_350_r210.txt",53));
        instances.add(new Instance("instances/MCDSP/n2500_350_r220.txt",50));
        instances.add(new Instance("instances/MCDSP/n2500_350_r230.txt",47));

        instances.add(new Instance("instances/MCDSP/n3000_400_r210.txt",73));
        instances.add(new Instance("instances/MCDSP/n3000_400_r220.txt",68));
        instances.add(new Instance("instances/MCDSP/n3000_400_r230.txt",63));
        instances.add(new Instance("instances/MCDSP/n3000_400_r240.txt",59));

        return instances;
    }

    public static void main(String[] args) throws IOException {

//        List<Instance> instances = calibration_instances();
//        List<Instance> instances = LMS();
//        List<Instance> instances = IEEE();
//        List<Instance> instances = MCDSP();
        List<Instance> instances = BOBL();
        int run = 10;

        BufferedWriter bw = new BufferedWriter(new FileWriter("res.csv", true));
        bw.write("\ninstance,");
        for(int i=0; i<10; ++i){
            bw.write("obj,time,iter,");
        }
        bw.write("best,best_time,best_iter,avg,avg_time\n");
        bw.close();

        for(Instance ins : instances){
            String[] tmp = ins.file_name.split("/");
            String ins_name = tmp[tmp.length-1];

            double best_time = Double.MAX_VALUE;
            int best_min_size = Integer.MAX_VALUE;
            int best_iter = Integer.MAX_VALUE;
            var res_list = new ArrayList<Integer>();
            var time_list = new ArrayList<Double>();

            bw = new BufferedWriter(new FileWriter("res.csv", true));
            bw.write(ins_name + ",");
            bw.close();

            for(int i=0; i<run; ++i) {

                System.out.println(ins_name + " run." + i);

                MCDSSolver solver = new MCDSSolver(ins.file_name);
                solver.solve(ins.lb);

                double time = solver.getTime();
                int min_size = solver.getMin_X_star().size();
                int iter = solver.getIter_count();
                res_list.add(min_size);
                time_list.add(time);
                if(min_size < best_min_size){
                    best_min_size = min_size;
                    best_time = time;
                    best_iter = iter;
                }else if(min_size == best_min_size && time < best_time){
                    best_time = time;
                    best_iter = solver.getIter_count();
                }
                bw = new BufferedWriter(new FileWriter("res.csv", true));
                bw.write(min_size + "," + time + "," + iter + ",");
                bw.close();
            }

            double avg = res_list.stream().mapToDouble(a->a).average().orElse(Double.NaN);
            double avg_time = time_list.stream().mapToDouble(a->a).average().orElse(Double.NaN);
            bw = new BufferedWriter(new FileWriter("res.csv", true));
            bw.write( + best_min_size + "," + best_time + "," + best_iter + "," + avg
                    + "," + avg_time + "\n");
            bw.close();
        }
    }
}
