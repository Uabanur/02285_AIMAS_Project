package dtu.aimas.search.solvers.graphsearch;

import java.util.HashSet;
import java.util.PriorityQueue;

import dtu.aimas.errors.NotImplemented;
import dtu.aimas.search.solvers.heuristics.Heuristic;
import dtu.aimas.search.solvers.heuristics.WAStarHeuristic;

public class FocalFrontier implements Frontier {
    private final PriorityQueue<State> open;
    private final PriorityQueue<State> focal;
    private final HashSet<State> set; 
    private final Heuristic heuristic;
    private final double w;
    private double fMin;

    public FocalFrontier(Heuristic h, int expectedStateSpaceSize){
        this.open = new PriorityQueue<>(expectedStateSpaceSize, h);
        this.focal = new PriorityQueue<>(expectedStateSpaceSize, h);
        this.set = new HashSet<>(expectedStateSpaceSize);
        this.heuristic = h;
        this.w = ((WAStarHeuristic)h).getW();
        this.fMin = Double.MAX_VALUE;
    }

    public void add(State state) {
        open.add(state);
        set.add(state);
        if(heuristic.f(state) <= w * fMin){
            focal.add(state);
        }
    }

    public boolean isEmpty() {
        return focal.isEmpty();
    }

    public boolean contains(State state) {
        return set.contains(state);
    }

    public State next() {
        var state = focal.poll();
        open.remove(state);
        set.remove(state);
        return state;
    }

    public void fillFocal() {
        double oldB = w * fMin;
        double newB = w * heuristic.f(open.peek());

        //todo: use priority queue instead of set (o(N) -> o(logN))
        for(var state: set){
            int f = heuristic.f(state); 
            if(f > oldB && f <= newB){
                focal.add(state);
            }
        }
    }

    public void updateFMin()  {
        this.fMin = heuristic.f(open.peek());
    }
}
