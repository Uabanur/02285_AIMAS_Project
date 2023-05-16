package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.search.solvers.heuristics.Heuristic;
import dtu.aimas.search.solvers.heuristics.WAStarHeuristic;

import java.util.HashSet;
import java.util.PriorityQueue;

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
        assert state != null;
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
        if (open.isEmpty()) return;
        assert focal.size() <= open.size() : "Focal should be a subset of open";

        if(fMin >= heuristic.f(open.peek())) return;
        double oldB = w * fMin;
        double newB = w * heuristic.f(open.peek());

        for(var state: open){
            int f = heuristic.f(state);
            if(f > oldB && f <= newB){
//                assert !focal.contains(state) : "State should not already be in focal";
                focal.add(state);
            }
        }
//        var openCopy = new PriorityQueue<>(open);
//        while(true){
//            if(openCopy.isEmpty()) break;
//            var state = openCopy.poll();
//            var f = heuristic.f(state);
//            // case: state already in focal -> skip
//            if(f <= oldB) continue;
//            // case: state exceeds new bound -> stop
//            if(f > newB) break;
//            // case: state within bound and not in focal -> add
//            assert !focal.contains(state) : "State should not already be in focal";
//            focal.add(state);
//        }
    }

    public void updateFMin()  {
        assert !open.isEmpty() : "Open should not be empty";
        this.fMin = heuristic.f(open.peek());
    }

    @Override
    public int size() {
        return open.size();
    }
}
