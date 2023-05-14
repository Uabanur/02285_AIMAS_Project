package dtu.aimas.search.solvers;

import dtu.aimas.common.Position;
import dtu.aimas.communication.IO;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.Attempt;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.safeinterval.ConflictInterval;
import dtu.aimas.search.solvers.safeinterval.SafePair;
import dtu.aimas.search.solvers.safeinterval.TimeInterval;

import java.util.*;

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

    public static List<StateSolution> getConflicts(StateSolution subject, List<StateSolution> candidates, StateSpace space){
        var conflicts = new ArrayList<StateSolution>();
        for(var other: candidates){
            if (subject == other)continue;
            var conflict = getConflict(subject, other, space);
            if(conflict.isEmpty()) continue;
            conflicts.add(conflict.get());
        }
        return conflicts;
    }

    public static Optional<StateSolution> getConflict(Attempt attempt, Attempt other, StateSpace space) {
        if (attempt.getSolution().isError() || other.getSolution().isError())
            return Optional.empty();

        var mainSolution = attempt.getSolution().get();
        var otherSolution = other.getSolution().get();
        return getConflict(mainSolution, otherSolution, space);
    }

    public static Optional<StateSolution> getConflict(StateSolution mainSolution, StateSolution otherSolution, StateSpace space) {

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

    public static Optional<ConflictInterval> getFirstConflictInterval(
            Set<Map.Entry<Problem, SafePair>> pairs, StateSpace space) {

        var merged = SolutionMerger.mergeSolutions(
                pairs.stream().map(p -> p.getValue().getSolution().get()).toList());

        var firstConflictStep = getFirstConflictStep(merged, space);
        if(firstConflictStep.isEmpty()) return Optional.empty();
        int intervalStart = firstConflictStep.get();
        List<StateSolution> conflictingSolutions = new ArrayList<>();

        var solutions = pairs.stream().map(p -> p.getValue().getSolution().get()).toList();
        for(var i = 0; i < solutions.size(); i++){
            for(var j = i+1; j < solutions.size(); j++){
                // find first solution which has conflicts with the others
                var main = solutions.get(i);
                var other = solutions.get(j);
                if (!checkConflictingAt(main, other, intervalStart, space)) continue;

                // collect all solutions which conflicts with the found solution.
                conflictingSolutions.add(main);
                conflictingSolutions.add(other);
                for(var k = j+1; k < solutions.size(); k++){
                    var remaining = solutions.get(k);
                    if(checkConflictingAt(main, remaining, intervalStart, space))
                        conflictingSolutions.add(remaining);
                }
                break;
            }
        }

        assert conflictingSolutions.size() > 0 : "Conflicts should be found at interval start";

        // we now have all solutions which conflict at the interval start
        Position conflictingCell = null;
        var overlapConflictResult = tryGetOverlapConflict(conflictingSolutions, intervalStart);
        if(overlapConflictResult.isPresent()){
            conflictingCell = overlapConflictResult.get();
        } else {
            var applicabilityConflictResult = tryGetApplicabilityConflict(conflictingSolutions, intervalStart, space);
            conflictingCell = applicabilityConflictResult.orElseThrow();
        }

        // we now have the conflicting cell
        // need to find the time step where none of the found solutions conflict anymore.
        for(var step = intervalStart+1;; step++){
            final int finalStep = step;
            var activeSolutions = conflictingSolutions.stream()
                    .filter(s -> s.size() > finalStep)
                    .toList();

            if(!checkConflictingAtTimeAndPosition(activeSolutions, step, conflictingCell, space)){
                var problems = pairs.stream().filter(
                        p -> conflictingSolutions.contains(p.getValue().getSolution().get())
                ).map(Map.Entry::getKey)
                .toList();

                return Optional.of(new ConflictInterval(problems, conflictingCell, new TimeInterval(intervalStart, step+1)));
            }
        }
    }


    // todo ugly duplicated code. deal with it
    private static boolean checkConflictingAtTimeAndPosition(List<StateSolution> solutions,
                     int step, Position position, StateSpace space){
        return tryGetOverlapConflictAt(solutions, step, position) ||
                tryGetApplicabilityConflictAt(solutions, step, position, space);
    }

    // todo ugly duplicated code. deal with it
    private static boolean tryGetApplicabilityConflictAt(
            List<StateSolution> conflictingSolutions, int intervalStart, Position position, StateSpace space) {

        var mergedState = space.shallowMerge(conflictingSolutions.stream().map(s -> s.getState(intervalStart-1)).toList());
        for(var solution: conflictingSolutions){
            var state = solution.getState(intervalStart);
            for(var i = 0; i < state.agents.size(); i++){
                var agent = state.parent.agents.get(i);
                var action = state.jointAction[i];
                var conflictingPosition = space.findConflictingPosition(mergedState, agent, action, intervalStart);
                if(conflictingPosition.isEmpty()) continue;
                if(conflictingPosition.get() == position) return true;
            }
        }

        return false;
    }

    // todo ugly duplicated code. deal with it
    private static boolean tryGetOverlapConflictAt(
            List<StateSolution> conflictingSolutions, int intervalStart, Position position) {
        var usedCells = new HashSet<Position>();
        for(var solution: conflictingSolutions){
            var state = solution.getState(intervalStart);
            for(var agent: state.agents) {
                if (!usedCells.add(agent.pos) && agent.pos == position) return true;
            }
            for(var box: state.boxes) {
                if (!usedCells.add(box.pos) && box.pos == position) return true;
            }
        }
        return false;
    }

    private static Optional<Position> tryGetApplicabilityConflict(
            List<StateSolution> conflictingSolutions, int intervalStart, StateSpace space) {

        var mergedState = space.shallowMerge(conflictingSolutions.stream().map(s -> s.getState(intervalStart-1)).toList());
        for(var solution: conflictingSolutions){
            var state = solution.getState(intervalStart);
            for(var i = 0; i < state.agents.size(); i++){
                var agent = state.parent.agents.get(i);
                var action = state.jointAction[i];
                var conflictingPosition = space.findConflictingPosition(mergedState, agent, action, intervalStart);
                if(conflictingPosition.isPresent()) return conflictingPosition;
            }
        }

        return Optional.empty();
    }

    private static Optional<Position> tryGetOverlapConflict(List<StateSolution> conflictingSolutions, int intervalStart) {
        var usedCells = new HashSet<Position>();
        for(var solution: conflictingSolutions){
            var state = solution.getState(intervalStart);
            for(var agent: state.agents) {
                if (!usedCells.add(agent.pos)) return Optional.of(agent.pos);
            }
            for(var box: state.boxes) {
                if (!usedCells.add(box.pos)) return Optional.of(box.pos);
            }
        }
        return Optional.empty();
    }

    private static boolean checkConflictingAt(StateSolution main,
          StateSolution remaining, int intervalStart, StateSpace space) {
        var solutions = List.of(main, remaining);
        return tryGetOverlapConflict(solutions, intervalStart).isPresent() ||
                tryGetApplicabilityConflict(solutions, intervalStart, space).isPresent();
    }


    private static Optional<Integer> getFirstConflictStep(StateSolution solution, StateSpace space){
        if(!space.isValid(solution.getState(0))) {
            IO.error("Conflict on initial states should not occur");
            return Optional.of(0);
        }

        for(var step = 1; step < solution.size(); step++) {
            var state = solution.getState(step);
            if (!space.isValid(state)) return Optional.of(step);
            for (var i = 0; i < state.agents.size(); i++) {
                if (!space.isApplicable(state.parent, state.parent.agents.get(i), state.jointAction[i])) {
                    return Optional.of(step);
                }
            }
        }

        return Optional.empty();
    }
}
