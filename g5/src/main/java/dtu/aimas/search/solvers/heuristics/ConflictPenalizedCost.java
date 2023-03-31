package dtu.aimas.search.solvers.heuristics;

import java.util.ArrayList;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.Plan;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class ConflictPenalizedCost implements Cost {

    private Cost baseCost;
    private Plan plan;

    public ConflictPenalizedCost(Cost baseCost, Plan plan){
        // TODO right now its a mix of StateSolution and ActionSolution. Clean it up.
        this.baseCost = baseCost;
        this.plan = plan;
    }

    @Override
    public int calculate(State state, StateSpace space) {
        var result = baseCost.calculate(state, space);

        var step = state.g();
        if(step == 0) return result;

        var problem = space.getProblem();
        var conflictPenalty = problem.walls.length * problem.walls[0].length;
        for (var conflictingSolution : plan.getConflicts()) {
            var solution = (StateSolution)conflictingSolution;
            if(solutionStepConflicts(state, space, solution, step)){
                result += conflictPenalty;
            }
        }

        return result;
    }

    private boolean solutionStepConflicts(State state, StateSpace space, 
        StateSolution solution, int step) {

        var solutionStep = Math.min(solution.size() - 1, step);
        var otherState = solution.getState(solutionStep);

        var currentState = combineState(state, otherState);
        if(!space.isValid(currentState)) return true;

        var prevMainState = state.parent;
        var prevOtherState = step < solution.size() ? otherState.parent : otherState;
        var previousState = combineState(prevMainState, prevOtherState);

        // TODO : Refactor duplicated logic from blackboard solver
        for(var i = 0; i < state.agents.size(); i++){
            if(space.isApplicable(previousState, state.parent.agents.get(i), state.jointAction[i])) continue;
            return true;
        }

        if(step < solution.size()){ // If other solution is finished its noop'ing
            for(var i = 0; i < otherState.agents.size(); i++){
                if(space.isApplicable(previousState, otherState.parent.agents.get(i), otherState.jointAction[i])) continue;
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
