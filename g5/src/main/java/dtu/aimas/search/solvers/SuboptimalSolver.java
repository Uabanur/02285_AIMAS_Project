package dtu.aimas.search.solvers;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;

public interface SuboptimalSolver extends Solver {
    Result<Solution> solve(Problem initial, double bound);
}
