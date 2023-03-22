package dtu.aimas.parsers;

import java.util.ArrayList;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class ProblemParser {
    public static Result<StateSpace> parse(Problem problem){
        return extractInitialState(problem)
            .map(init -> new StateSpace(problem, init));
    }

    private static Result<State> extractInitialState(Problem problem) {
        var agents = new ArrayList<>(problem.agents);
        var boxes = new ArrayList<>(problem.boxes);
        var initialState = new State(agents, boxes);
        return Result.ok(initialState);
    }
}
