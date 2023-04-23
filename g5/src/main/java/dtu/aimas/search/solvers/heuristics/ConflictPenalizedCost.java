package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.errors.UnreachableState;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.Attempt;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import java.util.ArrayList;

public class ConflictPenalizedCost implements Cost {

    private final Cost baseCost;
    private final Attempt attempt;

    public ConflictPenalizedCost(Cost baseCost, Attempt attempt){
        // TODO right now its a mix of StateSolution and ActionSolution. Clean it up.
        this.baseCost = baseCost;
        this.attempt = attempt;
    }

    @Override
    public int calculate(State state, StateSpace space) {
        var result = baseCost.calculate(state, space);

        var step = state.g();
        if(step == 0) return result;

        var problem = space.problem();

        // TODO: conflict should be much worse than unfinished boxes
        var conflictPenalty =  problem.mapSize();
        for (var conflictingSolution : attempt.getConflicts()) {
            if(solutionStepConflicts(state, space, conflictingSolution, step)){
                result += conflictPenalty;
            }
        }

        return result;
    }

    private boolean solutionStepConflicts(State state, StateSpace space, 
        StateSolution solution, int step) {

        var otherState = solution.getState(step);
        var currentState = combineState(state, otherState);
        if(!space.isValid(currentState)) {
            return true;
        }

        if(state.parent == null || state.jointAction == null) throw new UnreachableState();

        var prevMainState = state.parent;
        var prevOtherState = solution.getState(step-1);
        var previousState = combineState(prevMainState, prevOtherState);

        // TODO : Refactor duplicated logic from blackboard solver
        for(var i = 0; i < state.agents.size(); i++){
            if(space.isApplicable(previousState, state.parent.agents.get(i), state.jointAction[i])) continue;
            return true;
        }

        if(step < solution.size() && otherState.jointAction != null){ // If other solution is finished its noop'ing
            for(var i = 0; i < otherState.agents.size(); i++){
                if(space.isApplicable(previousState, prevOtherState.agents.get(i), otherState.jointAction[i])) continue;
                return true;
            }
        }

        return false;
    }

    private State combineState(State mainState, State otherState){
        var agents = new ArrayList<Agent>();
        var boxes = new ArrayList<Box>();
        agents.addAll(mainState.agents);
        boxes.addAll(mainState.boxes);

        agents.addAll(otherState.agents);
        boxes.addAll(otherState.boxes);

        return new State(agents, boxes);
    }
}
