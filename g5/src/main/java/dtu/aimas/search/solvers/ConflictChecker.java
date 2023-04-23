package dtu.aimas.search.solvers;

import dtu.aimas.errors.UnreachableState;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.Attempt;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConflictChecker {
    public static List<StateSolution> getConflicts(Attempt attempt, List<Attempt> attempts, StateSpace space) {
        var conflicts = new ArrayList<StateSolution>();
        for(var other: attempts){
            if (attempt == other)continue;
            var conflict = getConflict(attempt, other, space);
            if(conflict.isEmpty()) continue;
            conflicts.add(conflict.get());
        }
        return conflicts;
    }


    public static Optional<StateSolution> getConflict(Attempt attempt, Attempt other, StateSpace space) {
        if(attempt.getSolution().isError() || other.getSolution().isError())
            return Optional.empty();

        var mainSolution = attempt.getSolution().get();
        var otherSolution = other.getSolution().get();

        var maxSolutionLength = Math.max(mainSolution.size(), otherSolution.size());
        for(var step = 1; step < maxSolutionLength; step++){
            var mainState = mainSolution.getState(step);
            var otherState = otherSolution.getState(step);

            var prevMainState = mainSolution.getState(step-1);
            var prevOtherState = otherSolution.getState(step-1);

            var previousState = space.shallowMerge(prevMainState, prevOtherState);
            if(!space.isValid(previousState)) return Optional.of(otherSolution);

            if(step < mainSolution.size() && mainState.jointAction != null) { // If main solution is finished its NoOp
                for (var i = 0; i < mainState.agents.size(); i++) {
                    if (space.isApplicable(previousState, mainState.parent.agents.get(i), mainState.jointAction[i]))
                        continue;
                    return Optional.of(otherSolution);
                }
            }

            if(step < otherSolution.size() && otherState.jointAction != null){ // If other solution is finished its NoOp
                for(var i = 0; i < otherState.agents.size(); i++){
                    if(space.isApplicable(previousState, otherState.parent.agents.get(i), otherState.jointAction[i])) continue;
                    return Optional.of(otherSolution);
                }
            }
        }

        return Optional.empty();
    }

}
