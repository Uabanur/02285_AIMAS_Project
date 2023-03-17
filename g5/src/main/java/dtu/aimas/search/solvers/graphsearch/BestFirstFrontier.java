package dtu.aimas.search.solvers.graphsearch;

import java.util.HashSet;
import java.util.PriorityQueue;

public class BestFirstFrontier implements Frontier {
    private final PriorityQueue<State> queue;
    private final HashSet<State> set; 

    public BestFirstFrontier(Heuristic h, int expectedStateSpaceSize){
        this.queue = new PriorityQueue<>(expectedStateSpaceSize, h);
        this.set = new HashSet<>(expectedStateSpaceSize);
    }

    public BestFirstFrontier(Heuristic h) { 
        this(h, 2<<15);
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
        return queue.poll();
    }
}
