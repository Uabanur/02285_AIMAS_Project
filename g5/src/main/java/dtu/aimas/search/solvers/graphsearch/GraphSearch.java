package dtu.aimas.search.solvers.graphsearch;

import java.util.HashSet;

import dtu.aimas.common.Result;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import dtu.aimas.search.State;

public abstract class GraphSearch
{
    public Result<Solution> solve(Problem problem, Frontier frontier) 
    {
        return ProblemParser.parse(problem)
                .flatMap(init -> solve(problem, frontier, init));
    }
    
    private Result<Solution> solve(Problem problem, Frontier frontier, State initialState)
    {
        frontier.add(initialState);
        HashSet<State> expanded = new HashSet<>();

        while (true) 
        {
            if(frontier.isEmpty()) 
                return Result.error(new SolutionNotFound("Empty frontier"));

            State state = frontier.next();
            if(state.isGoalState(problem)) 
                return Result.ok(state.getSolution());

            expanded.add(state);
            for (State child : state.expand()) {
                if (!frontier.contains(child) && !expanded.contains(child)){
                    frontier.add(child);
                }
            }
        }
    }
}
