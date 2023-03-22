package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class GreedyHeuristic extends Heuristic {
    private Cost cost;

    public GreedyHeuristic(Cost cost) {
        this.cost = cost;
    }

    @Override
    public float f(State s, StateSpace space) {
        return cost.calculate(s, space);
    }
}
