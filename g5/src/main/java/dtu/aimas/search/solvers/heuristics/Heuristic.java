package dtu.aimas.search.solvers.heuristics;

import java.util.Comparator;
import java.util.HashMap;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public abstract class Heuristic implements Comparator<State> {
    private StateSpace space;
    private final HashMap<State, Integer> cache;

    protected Heuristic() {
        this.cache = new HashMap<>();
    }

    public StateSpace attachStateSpace(StateSpace space) {
        this.space = space;
        return space;
    }

    public abstract int f(State s, StateSpace space);
    public abstract Cost getCost();

    public void reset(){cache.clear();};
    public int compare(State fst, State snd) {
        return Integer.compare(f(fst), f(snd));
    }

    public int f(State s) {
        return cache.computeIfAbsent(s, state -> f(state, space));
    }
}