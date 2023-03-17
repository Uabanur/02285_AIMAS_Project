package dtu.aimas.search.solvers.graphsearch;

import java.util.Comparator;

public abstract class Heuristic implements Comparator<State> {
    private StateSpace space;
    public Heuristic(StateSpace space){
        this.space = space;
    }

    public abstract float f(State s, StateSpace space);

    public int compare(State fst, State snd) {
        return Float.compare(f(fst, space), f(snd, space));
    }
}
