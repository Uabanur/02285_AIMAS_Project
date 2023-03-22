package dtu.aimas.search.solvers.blackboard;

import dtu.aimas.common.Result;
import dtu.aimas.errors.NotImplemented;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class BlackboardSolver implements Solver {
    /* Ideas: 
        Rank boxes in order they need to be solved.
        They could be ranked such that if their goal blocks another box, they are ranked lower
    */

    public Result<Solution> solve(Problem problem) {
        return ProblemParser.parse(problem)
                .flatMap(space -> solve(space));
    }

    private Result<Solution> solve(StateSpace space) {
        // Solve agent naively initially
        // Go through each agent solution, and check if it conflicts with another agent
        // Make a heuristic to avoid using resources in the other agents plan
        // Solve both conflicting agents again with this heuristic. Hopefully finding non conflicting solutions

        return Result.error(new NotImplemented());
    }
}
