package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.heuristics.GreedyHeuristic;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public class Greedy extends GraphSearch implements Solver {
    private Heuristic heuristic;

    public Greedy(Cost cost){
        this.heuristic = new GreedyHeuristic(cost);
    }

    public Result<Solution> solve(Problem initial) {
        heuristic.reset();
        return solve(initial, heuristic);
    }
}
