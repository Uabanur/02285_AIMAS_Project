package dtu.aimas.parsers;

import java.util.ArrayList;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateConfig;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class ProblemParser {
    public static Result<StateSpace> parse(Problem problem){
        return parse(problem, new StateConfig());
    }

    public static Result<StateSpace> parse(Problem problem, StateConfig stateConfig){
        return extractInitialState(problem, stateConfig)
            .map(init -> new StateSpace(problem, init));
    }

    private static Result<State> extractInitialState(Problem problem, StateConfig stateConfig) {
        var agents = new ArrayList<>(problem.agents);
        var boxes = new ArrayList<>(problem.boxes);
        var initialState = new State(agents, boxes, stateConfig);
        return Result.ok(initialState);
    }
}
