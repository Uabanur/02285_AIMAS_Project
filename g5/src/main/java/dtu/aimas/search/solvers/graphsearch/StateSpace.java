package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;

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
}