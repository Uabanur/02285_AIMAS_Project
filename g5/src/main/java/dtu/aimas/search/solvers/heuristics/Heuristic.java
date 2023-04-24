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

    public int compare(State fst, State snd) {
        var fstCost = cache.computeIfAbsent(fst, s -> f(s, space));
//        fst.lastCost = fstCost; // TODO for debug
        var sndCost = cache.computeIfAbsent(snd, s -> f(s, space));
//        snd.lastCost = sndCost; // TODO for debug

        // prefer shorter goals if equal cost
        return fstCost.equals(sndCost)
                ? Integer.compare(fst.g(), snd.g())
                : Integer.compare(fstCost, sndCost);
    }
}