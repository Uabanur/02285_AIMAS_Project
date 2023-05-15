package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.SolverMinLength;
import dtu.aimas.search.solvers.heuristics.AStarHeuristic;
import dtu.aimas.search.solvers.heuristics.WAStarHeuristic;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public class Focal extends FocalGraphSearch implements Solver{
    private final Heuristic heuristic;

    public Focal(Cost cost, double w){
        this.heuristic = new WAStarHeuristic(cost, w);
    }

    @Override
    public Result<Solution> solve(Problem initial) {
        return solve(initial, heuristic);
    }
}
