package dtu.aimas.search.solvers.heuristics;

import java.util.Comparator;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public abstract class Heuristic implements Comparator<State> {
    private StateSpace space;
    public StateSpace attachStateSpace(StateSpace space) {
        this.space = space;
        return space;
    }

    public abstract float f(State s, StateSpace space);

    public int compare(State fst, State snd) {
        return Float.compare(f(fst, space), f(snd, space));
    }
}