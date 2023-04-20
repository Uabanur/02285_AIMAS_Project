package dtu.aimas.search.solvers.blackboard;

import dtu.aimas.common.Color;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.*;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.ConflictPenalizedCost;
import dtu.aimas.search.solvers.heuristics.Cost;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BlackboardSolver implements Solver {
    private final Cost baseCost;
    private final Function<Cost, SolverMinLength> subSolverGenerator;
    public BlackboardSolver(Function<Cost, SolverMinLength> subSolverGenerator, Cost baseCost){
        this.subSolverGenerator = subSolverGenerator;
        this.baseCost = baseCost;
    }

    public Result<Solution> solve(Problem problem) {
        return ProblemParser.parse(problem).flatMap(this::solve);
    }

    private Result<Solution> solve(StateSpace space) {
        IO.debug("Starting blackboard search");
        var initialState = space.initialState();
        var fullProblem = space.problem();

        // Solve sub problems naively initially
        var colors = initialState.agents.stream().map(a -> a.color)
                .collect(Collectors.toSet())
                .toArray(Color[]::new);

        var planCount = Math.max(1, colors.length);
        var plans = new Plan[planCount];
        if(colors.length == 0){
            IO.debug("No colors found. Initial state solution attempt with no actions.");
            var solution = new StateSolution(new State[]{initialState});
            plans[0] = new Plan(fullProblem, Result.ok(solution));
        } else {
            IO.debug("Found %d colors. Solving sub problems independently.", colors.length);
            var startTime = System.currentTimeMillis();
            for(var i = 0; i < plans.length; i++){
                var color = colors[i];
                var subProblem = ProblemSplitter.singleColor(fullProblem, color);
                var solution = subSolverGenerator.apply(this.baseCost)
                        .solve(subProblem)
                        .map(s -> (StateSolution)s);

                plans[i] = new Plan(subProblem, solution);
            }
            var endTime = System.currentTimeMillis();
            IO.debug("Average time per naive solution: %,.2f ms", (endTime-startTime)/(float)planCount);
        }

        IO.debug("Searching for conflict free solution permutations");

        var set = new HashSet<AttemptPermutation>();
        var queue = new PriorityQueue<AttemptPermutation>();
        queue.add(new AttemptPermutation(new int[planCount], plans, space));

        while(true){

            // Verify that we can continue
            if(queue.isEmpty()){
                IO.error("All unique solution combinations have been exhausted");
                return Result.error(new SolutionNotFound("No more attempt permutations"));
            }

            // Check if goal is found
            var attemptPermutation = queue.poll();
            var attempts = attemptPermutation.getAttempts(plans);
            IO.debug("Next solution permutation: %s", attemptPermutation);

            if(SolutionChecker.validAttempts(attempts, space)){
                return Result.ok(SolutionMerger.mergeAttempts(attempts));
            }

            // Calculate all neighbor attempts
            for(var i = 0; i < planCount; i++) {
                if(attempts.get(i).getConflicts().isEmpty()) continue;

                var attemptSolutionLength = 0;
                for(var j = 0; j < planCount; j++){
                    if(i == j) continue;
                    var solutionSize = attempts.get(j).getSolution().map(StateSolution::size).getOrElse(() -> 0);
                    if(solutionSize > attemptSolutionLength) attemptSolutionLength = solutionSize;
                }

                var solution = subSolverGenerator
                        .apply(new ConflictPenalizedCost(baseCost, attempts.get(i)))
                        .solve(plans[i].getProblem(), attemptSolutionLength)
                        .map(s -> (StateSolution)s);
                plans[i].addAttempt(new Attempt(solution));

                var next = attemptPermutation.transfer(i, plans[i].lastAttemptIndex(), plans, space);
                if (set.contains(next)) continue;
                queue.add(next);
                set.add(next);
            }
        }
    }


    public List<int[]> combinations(int[] options, int freezePosition, int freezeValue){
        var result = new ArrayList<int[]>();

        var permutation = new int[options.length];
        permutation[freezePosition] = freezeValue;
        var mutableOptions = IntStream.range(0, options.length)
                .filter(i -> i != freezePosition).toArray();

        while(true){
            result.add(permutation.clone());
            for(var i: mutableOptions){
                if(permutation[i] < options[i] - 1){
                    ++permutation[i];
                    break;
                }
                else{
                    permutation[i] = 0;
                    if(i == mutableOptions[mutableOptions.length-1]){
                        return result;
                    }
                }
            }
        }
    }
}

