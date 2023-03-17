package dtu.aimas.search.solvers.graphsearch;

import java.util.HashSet;

import dtu.aimas.common.Result;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;

public abstract class GraphSearch
{
    public Result<Solution> solve(Problem problem, Frontier frontier) 
    {
        return ProblemParser.parse(problem)
                .flatMap(space -> solve(space, frontier));
    }
    
    private Result<Solution> solve(StateSpace space, Frontier frontier)
    {
        frontier.add(space.getInitialState());
        HashSet<State> expanded = new HashSet<>();

        while (true) 
        {
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
}
