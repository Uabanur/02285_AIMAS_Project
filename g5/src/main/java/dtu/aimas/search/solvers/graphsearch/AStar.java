package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import dtu.aimas.search.solvers.Solver;

public class AStar extends GraphSearch implements Solver {
    private Frontier frontier;

    public AStar(Heuristic heuristic, int expectedStateSpaceSize){
        this.frontier = new BestFirstFrontier(heuristic, expectedStateSpaceSize);
    }

    public AStar(Heuristic heuristic){
        this.frontier = new BestFirstFrontier(heuristic);
    }

    public Result<Solution> solve(Problem initial) {
        return solve(initial, frontier);
    }
}
