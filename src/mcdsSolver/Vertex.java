package mcdsSolver;

public class Vertex implements Comparable{
    final int index;
    int degree_to_X_star = 0;
    boolean is_in_X_star = false;
    boolean is_cut = false;
    int weight = 1;
    int birth_iter = 0;
    int tabu_tenure = -1;

    //used for determining cutting point
    boolean is_visited = false;
    int dep = 0;
    int low = 0;

    //used for initialization
    int d;

    public Vertex(int i) {index = i;}

    @Override
    public int compareTo(Object o) {
        if (o == null){
            throw new NullPointerException();
        }
        if(o.getClass() != Vertex.class){
            throw new ClassCastException();
        }

        Vertex oo = (Vertex)o;
        return Integer.compare(index, oo.index);
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            throw new NullPointerException();
        }
        if(o.getClass() != Vertex.class){
            throw new ClassCastException();
        }

        Vertex oo = (Vertex)o;
        return index == oo.index;
    }

    @Override
    public String toString(){
        return String.valueOf(index);
    }
}
