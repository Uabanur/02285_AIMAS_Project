package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import lombok.Getter;
import lombok.NonNull;

public class AStarHeuristic extends Heuristic {
    @Getter
    private final Cost cost;

    public AStarHeuristic(@NonNull Cost cost) {
        this.cost = cost;
    }

    @Override
    public int f(State s, StateSpace space) {
        return s.g() + cost.calculate(s, space);
    }
}
