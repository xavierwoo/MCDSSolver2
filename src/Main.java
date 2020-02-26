import mcdsSolver.MCDSSolver;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        MCDSSolver solver = new MCDSSolver("instances/MCDSP/n2500_350_r210.txt");
        solver.solve(45);
//        var hit_time = new ArrayList<Double>();
//        for(int i=0; i<10; ++i) {
//            MCDSSolver solver = new MCDSSolver("instances/IEEE/ieee_300_bus.txt");
//            boolean res = solver.solve(129);
//            if(res){
//                hit_time.add(solver.getTime());
//            }
//        }
//        System.out.println(hit_time);
    }
}
