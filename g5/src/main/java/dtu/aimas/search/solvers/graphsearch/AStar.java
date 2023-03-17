package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import dtu.aimas.search.solvers.Solver;

public class AStar extends GraphSearch implements Solver {
    private Heuristic heuristic;

    public AStar(Heuristic heuristic){
        this.heuristic = heuristic;
    }

    public Result<Solution> solve(Problem initial) {
        return solve(initial, heuristic);
    }
}
