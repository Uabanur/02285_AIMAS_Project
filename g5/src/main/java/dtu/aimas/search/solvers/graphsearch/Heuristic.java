package dtu.aimas.search.solvers.graphsearch;

import java.util.Comparator;

import dtu.aimas.search.State;

public abstract class Heuristic implements Comparator<State> {
    public abstract float f(State s);

    public int compare(State fst, State snd) {
        return Float.compare(f(fst), f(snd));
    }
}
