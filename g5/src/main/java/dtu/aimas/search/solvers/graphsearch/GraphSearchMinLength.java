package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.heuristics.Heuristic;

import java.util.HashSet;

public abstract class GraphSearchMinLength {

    public Result<Solution> solve(Problem problem, Heuristic heuristic, int minSolutionLength) {
        return ProblemParser.parse(problem)
                .map(heuristic::attachStateSpace)
                .flatMap(space -> solve(space, new BestFirstFrontier(heuristic, problem.expectedStateSize), minSolutionLength));
    }

    public Result<Solution> solve(Problem problem, BasicFrontier frontier, int minSolutionLength)
    {
        return ProblemParser.parse(problem)
                .flatMap(space -> solve(space, frontier, minSolutionLength));
    }

    private Result<Solution> solve(StateSpace space, Frontier frontier, int minSolutionLength)
    {
        frontier.add(space.getInitialState());
        HashSet<State> expanded = new HashSet<>();

        while (true)
        {
            if(frontier.isEmpty())
                return Result.error(new SolutionNotFound("Empty frontier"));

            State state = frontier.next();
            if(state.g() >= minSolutionLength && space.isGoalState(state))
                return space.createSolution(state);

            expanded.add(state);
            for (State child : space.expand(state)) {
                if (!frontier.contains(child) && !expanded.contains(child)){
                    frontier.add(child);
                }
            }
        }
    }
}
