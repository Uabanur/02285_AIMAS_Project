package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class DefaultCost implements Cost{
    private DefaultCost(){}
    public static Cost instance(){
        return new DefaultCost();
    }
    @Override
    public int calculate(State state, StateSpace space) {
        return 0;
    }
}
