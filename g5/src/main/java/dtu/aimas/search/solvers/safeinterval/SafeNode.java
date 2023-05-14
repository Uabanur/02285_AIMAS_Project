package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.ConflictChecker;
import dtu.aimas.search.solvers.SolutionMerger;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SafeNode implements Comparable<SafeNode> {
    private int cost;
    private final Map<Problem, SafePair> solutions;

    private SafeNode(Map<Problem, SafePair> solutions){
        this.solutions = solutions;
    }
    public SafeNode(){
        this(new HashMap<>());
    }

    public void bind(Problem problem, Result<StateSolution> solution) {
        assert !(problem instanceof SafeProblem): "Only bind original problem";

        var binding = solutions.get(problem);
        if (binding != null){
            binding.setSolution(solution);
            return;
        }

        var safeProblem = SafeProblem.from(problem);
        solutions.put(problem, new SafePair(safeProblem, solution));
    }

    private Result<List<StateSolution>> collapse(){
        return Result.collapse(solutions.values().stream().map(SafePair::getSolution).toList());
    }

    public void calculateCost() {
        var flatSolutions = collapse();
        if(flatSolutions.isError()){
            cost = -1;
            return;
        }

        cost = flatSolutions.get().stream()
                .mapToInt(StateSolution::getMakespan)
                .max()
                .orElse(0);
    }

    public Optional<ConflictInterval> firstConflict(StateSpace space){
        var pairs = solutions.entrySet();
        var conflictCheck = Result.collapse(pairs.stream().map(p -> p.getValue().getSolution()).toList());
        assert conflictCheck.isOk() : conflictCheck.getError().getMessage();

        return ConflictChecker.getFirstConflictInterval(pairs, space);
    }

    @Override
    public int compareTo(SafeNode o) {
        return Integer.compare(this.cost, o.cost);
    }

    public Result<StateSolution> merge() {
        return collapse().map(SolutionMerger::mergeSolutions);
    }

    public Optional<SafeNode> restrict(Problem problem, ConflictInterval conflictInterval) {
        // todo maybe check interval before cloning

        // clone the updated map entry
        var safePairCopy = solutions.get(problem).copy();
        var safeProblemResult = SafeProblem.from(safePairCopy.getSafeProblem(), conflictInterval);
        if(safeProblemResult.isEmpty()) return Optional.empty();

        // update with the new restrictions
        safePairCopy.setSafeProblem(safeProblemResult.get());

        // shallow copy of the solutions map
        var child = new SafeNode(new HashMap<>(solutions));

        // update the single changed entry
        child.solutions.put(problem, safePairCopy);
        return Optional.of(child);
    }

    public SafeProblem safeProblemFor(Problem problem) {
        return solutions.get(problem).getSafeProblem();
    }

    public boolean isSolvable() {
        return cost >= 0;
    }

    public boolean isEmpty() {
        return solutions.isEmpty();
    }
}
