package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;

public class BFS extends GraphSearch implements Solver 
{
    public Result<Solution> solve(Problem initial) {
        return solve(initial, BasicFrontier.fifo());
    }
}
