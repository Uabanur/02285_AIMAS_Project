package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;
import java.util.Optional;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.errors.InvalidOperation;
import dtu.aimas.search.ActionSolution;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import lombok.Getter;

public class StateSpace {
    @Getter
    private Problem problem;

    @Getter
    private State initialState;

    public StateSpace(Problem problem, State initialState) {
        this.problem = problem;
        this.initialState = initialState;
    }

    public Result<Solution> createSolution(State state){
        if (!isGoalState(state)) 
            return Result.error(new InvalidOperation("Can only create a solution from a goal state"));

        return Result.ok(new ActionSolution(state.extractPlan()));
    }

    public boolean isGoalState(State state) {
        // TODO : change this to use the problem from the state space
        return state.isGoalState();
    }

    public ArrayList<State> expand(State state) {
        // TODO : this functionality belongs to the state space
        return state.getExpandedStates();
    }

    public Optional<Agent> getAgentByNumber(State state, int i) {
        if (i >= state.agentRows.length) return Optional.empty();
        return Optional.of(
                new Agent(new Position(state.agentRows[i], state.agentCols[i]), State.agentColors[i]));
    }

    public Optional<Box> getBoxAt(State state, int row, int col) {
        var symbol = state.boxes[row][col];
        if (!Box.isLabel(symbol)) return Optional.empty();
        var color = State.boxColors[symbol-'A'];
        return Optional.of(new Box(new Position(row, col), color, symbol));
    }
}