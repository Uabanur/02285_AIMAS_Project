package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class AStarHeuristic extends Heuristic {

    private Cost cost;

    public AStarHeuristic(Cost cost) {
        this.cost = cost;
    }

    @Override
    public float f(State s, StateSpace space) {
        return s.g() + cost.calculate(s, space);
    }
}
