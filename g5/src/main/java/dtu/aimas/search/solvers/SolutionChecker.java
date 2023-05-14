package dtu.aimas.search.solvers;

import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.Attempt;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import java.util.List;

public class SolutionChecker {

    public static boolean validAttempts(List<Attempt> attempts, StateSpace space){
        for(var attempt: attempts){
            if (attempt.getSolution().isError()) return false;
            if (!attempt.getConflicts().isEmpty()) return false;
        }

        var mergedSolution = SolutionMerger.mergeAttempts(attempts);
        return SolutionChecker.validSolution(mergedSolution, space);
    }

    public static boolean validSolution(StateSolution solution, StateSpace space){
        var expectedInitialState = space.initialState();
        var givenInitialState = solution.getState(0);

        // initial state cannot have a joint action
        if(givenInitialState.jointAction != null) return false;

        // Start should be equivalent to the initial state from the state space
        if(!givenInitialState.equivalent(expectedInitialState)) return false;

        // each state must be from the action of the parent state, and valid
        var agentCount = givenInitialState.agents.size();
        for(var step = 1; step < solution.size(); step ++){
            var state = solution.getState(step);
            if(!space.isValid(state) || state.jointAction == null) return false;

            if(state.jointAction.length != agentCount) return false;
            var parent = state.parent;
            if(parent == null) return false;
            for(var i = 0; i < agentCount; i++){
                var agent = parent.agents.get(i);
                var action = state.jointAction[i];
                if(!space.isApplicable(parent, agent, action)) return false;

                // Applying the joint action to the parent state should be equivalent to this state
                var appliedState = space.tryCreateState(parent, state.jointAction);
                if(!appliedState.map(state::equivalent).orElse(false)) return false;
            }
        }

        // final state must be a goal state
        return space.isGoalState(solution.getState(solution.size()-1));
    }
}
