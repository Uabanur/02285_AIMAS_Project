package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import lombok.Getter;
import lombok.NonNull;

public class WAStarHeuristic extends Heuristic {
    @Getter
    private final Cost cost;
    @Getter
    private final double w;

    public WAStarHeuristic(@NonNull Cost cost, double w) {
        this.cost = cost;
        this.w = w;
    }

    @Override
    public int f(State s, StateSpace space) {
        return (int)(s.g() + w * cost.calculate(s, space));
    }
}
