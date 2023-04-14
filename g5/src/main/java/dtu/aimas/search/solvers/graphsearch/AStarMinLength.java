package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.SolverMinLength;
import dtu.aimas.search.solvers.heuristics.AStarHeuristic;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public class AStarMinLength extends GraphSearchMinLength implements SolverMinLength {
    private Heuristic heuristic;

    public AStarMinLength(Cost cost){
        this.heuristic = new AStarHeuristic(cost);
    }

    public Result<Solution> solve(Problem initial, int minSolutionLength) {
        return solve(initial, heuristic, minSolutionLength);
    }

    @Override
    public Result<Solution> solve(Problem initial) {
        return solve(initial, 0);
    }
}
