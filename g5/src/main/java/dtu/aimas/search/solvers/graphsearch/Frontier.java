package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.search.State;

public interface Frontier 
{
    void add(State initialState);
    boolean isEmpty();
    boolean contains(State child);
    State next();
}
