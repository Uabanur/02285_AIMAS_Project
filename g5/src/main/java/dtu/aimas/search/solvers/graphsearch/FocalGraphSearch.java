package dtu.aimas.search.solvers.graphsearch;

import java.util.HashSet;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.heuristics.Heuristic;

public abstract class FocalGraphSearch
{
    public Result<Solution> solve(Problem problem, Heuristic heuristic, double w) {
        return ProblemParser.parse(problem)
            .map(heuristic::attachStateSpace)
            .flatMap(space -> solve(space, new FocalFrontier(heuristic, problem.expectedStateSize, w)));
    }

    // public Result<Solution> solve(Problem problem, BasicFrontier frontier, double w) 
    // {
    //     return ProblemParser.parse(problem)
    //             .flatMap(space -> solve(space, frontier, w));
    // }
    
    private Result<Solution> solve(StateSpace space, FocalFrontier frontier)
    {
        frontier.add(space.initialState());
        HashSet<State> expanded = new HashSet<>();

        while (true) 
        {
            IO.info("expanding...");
            frontier.updateFMin();

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
            
            frontier.fillFocal();
        }
    }
}
