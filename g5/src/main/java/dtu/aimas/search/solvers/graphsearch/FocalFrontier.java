package dtu.aimas.search.solvers.graphsearch;

import java.util.HashSet;
import java.util.PriorityQueue;

import dtu.aimas.errors.NotImplemented;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public class FocalFrontier implements Frontier {
    private final PriorityQueue<State> open;
    private final PriorityQueue<State> focal;
    private final HashSet<State> set; 
    private final Heuristic heuristic;
    private final double w;
    private double fMin;

    public FocalFrontier(Heuristic h, int expectedStateSpaceSize, double w){
        this.open = new PriorityQueue<>(expectedStateSpaceSize, h);
        this.focal = new PriorityQueue<>(expectedStateSpaceSize, h);
        this.set = new HashSet<>(expectedStateSpaceSize);
        this.heuristic = h;
        this.w = w;
        this.fMin = Double.POSITIVE_INFINITY;
    }

    // public FocalFrontier(Heuristic h) { 
    //     this(h, 2<<15);
    // }

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
        var openCopy = new PriorityQueue<>(open);
        if(!open.isEmpty() && fMin < heuristic.f(open.peek())){
            while(true){
                var state = openCopy.poll();
                if(openCopy.isEmpty() || heuristic.f(state) <= oldB || heuristic.f(state) > newB) break;
                focal.add(state);
            }
        }
    }

    public void updateFMin()  {
        this.fMin = heuristic.f(open.peek());
    }
}
