package dtu.aimas.parsers;

import dtu.aimas.common.Color;
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
        // TODO: Make the state nicer.

        var agentRows = problem.agents.stream().mapToInt(a -> a.pos.row).toArray();
        var agentCols = problem.agents.stream().mapToInt(a -> a.pos.col).toArray();

        var agentColors = problem.agents.stream().map(a -> a.color).toArray(Color[]::new);
        var boxColors = problem.boxes.stream().map(b -> b.color).toArray(Color[]::new);

        var boxes = new char[problem.walls.length][problem.walls[0].length];
        for(var box : problem.boxes) boxes[box.pos.row][box.pos.col] = box.type;

        var state = new State(agentRows, agentCols, agentColors, problem.walls, boxes, boxColors, problem.goals);
        return Result.ok(state);
    }
}
