package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import dtu.aimas.search.solvers.Solver;

public class DFS extends GraphSearch implements Solver
{
    public Result<Solution> solve(Problem initial) {
        return solve(initial, BasicFrontier.filo());
    }
}
