package mcdsSolver;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Graph {
    private List<List<Vertex>> connection_info = new ArrayList<>();
    private List<Vertex> vertices_set = new ArrayList<>();

    private boolean[][] con_matrix;

    public void render_matrix(){
        int len = maxVertexIndex() + 1;
        con_matrix = new boolean[len][len];

        for(int i=0; i<con_matrix.length; ++i){
            for(int j=0; j<con_matrix[i].length; ++j){
                con_matrix[i][j] = false;
            }
        }

        for(Vertex a : vertices_set){
            for(Vertex b : neighborsOf(a)){
                con_matrix[a.index][b.index] = true;
            }
        }
    }

    public void addVertex(Vertex v){
        while(connection_info.size()-1 < v.index){
            connection_info.add(null);
        }

        if(connection_info.get(v.index) != null){
            throw new Error("Vertex index occupied");
        }

        connection_info.set(v.index, new ArrayList<>());
        vertices_set.add(v);
    }

    public void addEdge(Vertex a, Vertex b){
        List<Vertex> a_edges = connection_info.get(a.index);
        a_edges.add(b);
        List<Vertex> b_edges = connection_info.get(b.index);
        b_edges.add(a);
    }

    public List<Vertex> neighborsOf(Vertex v){
        return connection_info.get(v.index);
    }

    public List<Vertex> vertexSet(){
        return vertices_set;
    }

    public int degreeOf(Vertex v){
        return connection_info.get(v.index).size();
    }

    public boolean containsEdge(Vertex a, Vertex b){
        //return connection_info.get(a.index).contains(b);
        return con_matrix[a.index][b.index];
    }

    public int maxVertexIndex(){
        int m_id = 0;
        for(Vertex v : vertices_set){
            if(v.index > m_id){
                m_id = v.index;
            }
        }

        return m_id;

    }
}
