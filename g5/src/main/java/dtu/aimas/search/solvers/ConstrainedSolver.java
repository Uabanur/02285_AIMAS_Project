package dtu.aimas.search.solvers;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;

public interface ConstrainedSolver extends Solver {
    Result<Solution> solve(Problem initial, Constraint constraint);
}
