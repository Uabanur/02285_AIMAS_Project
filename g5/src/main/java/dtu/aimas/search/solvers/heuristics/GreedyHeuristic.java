package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import lombok.Getter;
import lombok.NonNull;

public class GreedyHeuristic extends Heuristic {
    @Getter
    private final Cost cost;

    public GreedyHeuristic(@NonNull Cost cost) {
        this.cost = cost;
    }

    @Override
    public int f(State s, StateSpace space) {
        return cost.calculate(s, space);
    }
}
