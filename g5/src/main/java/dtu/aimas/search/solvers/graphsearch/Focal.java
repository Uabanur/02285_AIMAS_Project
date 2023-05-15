package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.SolverMinLength;
import dtu.aimas.search.solvers.SuboptimalSolver;
import dtu.aimas.search.solvers.heuristics.AStarHeuristic;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public class Focal extends FocalGraphSearch implements SuboptimalSolver{
    private final Heuristic heuristic;

    public Focal(Cost cost){
        this.heuristic = new AStarHeuristic(cost);
    }

    @Override
    public Result<Solution> solve(Problem initial) {
        return solve(initial, 1.1);
    }

    @Override
    public Result<Solution> solve(Problem initial, double bound) {
        return solve(initial, heuristic, bound);
    }
}
