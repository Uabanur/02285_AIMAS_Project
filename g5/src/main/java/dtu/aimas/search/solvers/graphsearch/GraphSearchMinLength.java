package dtu.aimas.search.solvers.graphsearch;

import dtu.aimas.common.Result;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Action;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.heuristics.Heuristic;

import java.util.HashSet;
import java.util.stream.IntStream;

public abstract class GraphSearchMinLength {
    private Heuristic heuristic = null;
    private Action[] noopAction;

    public Result<Solution> solve(Problem problem, Heuristic heuristic, int minSolutionLength) {
        this.heuristic = heuristic;
        this.noopAction = IntStream.range(0, problem.agents.size()).mapToObj(a -> Action.NoOp).toArray(Action[]::new);
        return ProblemParser.parse(problem)
                .map(heuristic::attachStateSpace)
                .flatMap(space -> solve(space, new BestFirstFrontier(heuristic, problem.expectedStateSize), minSolutionLength));
    }

    public Result<Solution> solve(Problem problem, BasicFrontier frontier, int minSolutionLength)
    {
        this.noopAction = IntStream.range(0, problem.agents.size()).mapToObj(a -> Action.NoOp).toArray(Action[]::new);
        return ProblemParser.parse(problem)
                .flatMap(space -> solve(space, frontier, minSolutionLength));
    }

    private Result<Solution> solve(StateSpace space, Frontier frontier, int minSolutionLength)
    {
        var init = space.initialState();
        frontier.add(init);
        var evaluated = new HashSet<State>();
        evaluated.add(init);


        while (true)
        {
            if(frontier.isEmpty())
                return Result.error(new SolutionNotFound("Empty frontier"));

            State state = frontier.next();
            if(space.isGoalState(state)){
                // If a short path is found which has future problems, expanding it now will block the goal state for
                // other paths, therefore this state is ignored.
                if(state.g() + 1 < minSolutionLength && hasFutureConflicts(state, space, minSolutionLength)) continue;

                // pad solution with NoOp's
                while(state.g() + 1 < minSolutionLength) {
                    state = staticChild(state);
                }

                return space.createSolution(state);
            }

            for (State child : space.expand(state)) {
//                if(evaluated.containsKey(child)){
//                    var prev = evaluated.get(child);
//                    if(heuristic.compare(child, prev) > 0){
//
////                    }
////                    if(cost.calculate(child, space) < cost.calculate(prev, space)){
//                        evaluated.remove(prev);
//                        frontier.clear(prev);
//                    }
//                }

                if (!evaluated.contains(child)){
                    frontier.add(child);
                    evaluated.add(child);
                }
            }
        }
    }

    private boolean hasFutureConflicts(State state, StateSpace space, int minSolutionLength){
        var current = state;
        var cost = heuristic.getCost();
        for(var step = state.g(); step <= minSolutionLength; step++){
            if(cost.calculate(current, space) > 0) return true;
            current = staticChild(current);
        }
        return false;
    }

    private State staticChild(State parent) {
        return new State(parent, parent.agents, parent.boxes, noopAction);
    }
}
