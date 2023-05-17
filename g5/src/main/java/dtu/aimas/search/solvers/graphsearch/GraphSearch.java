package dtu.aimas.search.solvers.graphsearch;

import java.util.HashSet;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public abstract class GraphSearch
{
    public Result<Solution> solve(Problem problem, Heuristic heuristic) {
        return ProblemParser.parse(problem)
            .map(heuristic::attachStateSpace)
            .flatMap(space -> solve(space, new BestFirstFrontier(heuristic, problem.expectedStateSize)));
    }

    public Result<Solution> solve(Problem problem, BasicFrontier frontier) 
    {
        return ProblemParser.parse(problem)
                .flatMap(space -> solve(space, frontier));
    }

    private static long startTime = System.nanoTime();
    
    private Result<Solution> solve(StateSpace space, Frontier frontier)
    {
        frontier.add(space.initialState());
        HashSet<State> expanded = new HashSet<>();

        int iterations = 0;
        startTime = System.nanoTime();
        while (true) 
        {
            //Print a status message every 10000 iteration
            if (++iterations % 10000 == 0) {
                IO.spam(getSearchStatus(expanded, frontier));
            }

            if(frontier.isEmpty()) 
                return Result.error(new SolutionNotFound("Empty frontier"));

            State state = frontier.next();
            if(space.isGoalState(state)) 
                return space.createSolution(state);

            expanded.add(state);
            for (State child : space.expand(state)) {
                if (!frontier.contains(child) && !expanded.contains(child)){
                    frontier.add(child);
                }
            }
        }
    }

    private static String getSearchStatus(HashSet<State> expanded, Frontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        return String.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(), elapsedTime);
    }
}
