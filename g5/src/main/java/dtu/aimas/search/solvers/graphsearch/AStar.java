package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.heuristics.AStarHeuristic;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public class AStar extends GraphSearch implements Solver {
    private Heuristic heuristic;

    public AStar(Cost cost){
        this.heuristic = new AStarHeuristic(cost);
    }

    public Result<Solution> solve(Problem initial) {
        return solve(initial, heuristic);
    }
}
