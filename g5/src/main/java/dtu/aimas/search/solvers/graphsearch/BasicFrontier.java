package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayDeque;
import java.util.HashSet;

public class BasicFrontier implements Frontier {
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    private final boolean fifo;
    private BasicFrontier(boolean fifo){
        this.fifo = fifo;
    }

    public static BasicFrontier fifo(){
        return new BasicFrontier(true);
    }
    public static BasicFrontier filo(){
        return new BasicFrontier(false);
    }

    public void add(State state) {
        queue.add(state);
        set.add(state);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean contains(State state) {
        return set.contains(state);
    }

    public State next() {
        var state = this.fifo ? queue.pollFirst() : queue.pollLast();
        set.remove(state);
        return state;
    }
}
