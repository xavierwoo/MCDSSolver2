package mcdsSolver;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;


/***
 * Only for the sets with same size
 */
public class Comp implements Comparator<Set<Vertex>> {
    @Override
    public int compare(Set<Vertex> s1, Set<Vertex> s2) {
        Iterator<Vertex> s1_iter = s1.iterator();
        Iterator<Vertex> s2_iter = s2.iterator();

        while(s1_iter.hasNext()){
            Vertex s1v = s1_iter.next();
            Vertex s2v = s2_iter.next();
            int cmp = s1v.compareTo(s2v);
            if (cmp != 0) return cmp;
        }

        return 0;
    }
}
