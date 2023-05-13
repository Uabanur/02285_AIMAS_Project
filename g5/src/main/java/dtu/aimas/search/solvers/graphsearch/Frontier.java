package dtu.aimas.search.solvers.graphsearch;

public interface Frontier 
{
    void add(State initialState);
    boolean isEmpty();
    boolean contains(State child);
    State next();
    int size();
}
