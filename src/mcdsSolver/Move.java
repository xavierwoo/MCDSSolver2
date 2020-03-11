package mcdsSolver;

public class Move {
    Vertex insert_v;
    Vertex remove_v;

    int delta_f = Integer.MAX_VALUE;
    int delta_X_minu_size = Integer.MAX_VALUE;
    int delta_risk_weight = Integer.MAX_VALUE;
    int sum_age = Integer.MAX_VALUE;

    Move(Vertex iv, Vertex rv, int df){
        insert_v = iv;
        remove_v = rv;
        delta_f = df;
    }

    Move(Vertex iv, Vertex rv){
        insert_v = iv;
        remove_v = rv;
    }
}
