package dtu.aimas.search.solvers.blackboard;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.problems.ProblemSplitter;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.SolutionChecker;
import dtu.aimas.search.solvers.SolutionMerger;
import dtu.aimas.search.solvers.SolverMinLength;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.ConflictPenalizedCost;
import dtu.aimas.search.solvers.heuristics.Cost;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.function.Function;

public class BlackboardSolver implements SolverMinLength {
    private final Cost baseCost;
    private final ProblemSplitter problemSplitter;
    private final Function<Cost, SolverMinLength> subSolverGenerator;
    public BlackboardSolver(
            ProblemSplitter problemSplitter,
            Function<Cost, SolverMinLength> subSolverGenerator,
            Cost baseCost){
        this.problemSplitter = problemSplitter;
        this.subSolverGenerator = subSolverGenerator;
        this.baseCost = baseCost;
    }

    @Override
    public Result<Solution> solve(Problem initial) {
        return solve(initial, 0);
    }
    public Result<Solution> solve(Problem problem, int minSolutionLength) {
        return ProblemParser.parse(problem).flatMap(space -> solve(space, minSolutionLength));
    }

    private Result<Solution> solve(StateSpace space, int minSolutionLength) {
        IO.debug("Starting blackboard search");
        var initialState = space.initialState();
        var problem = space.problem();

        var subProblems = problemSplitter.split(problem);
        var planCount = Math.max(1, subProblems.size());
        var plans = new Plan[planCount];

        IO.debug("Received %d sub problems. Solving independently.", subProblems.size());
        var startTime = System.currentTimeMillis();
        for(var i = 0; i < subProblems.size(); i++){
            var subProblem = subProblems.get(i);
            var solution = subSolverGenerator.apply(this.baseCost)
                    .solve(subProblem, minSolutionLength)
                    .map(s -> (StateSolution) s);

            plans[i] = new Plan(subProblem, solution);
        }
        var endTime = System.currentTimeMillis();
        IO.debug("Average time per naive solution: %,.2f ms", (endTime - startTime) / (float) planCount);

        if(subProblems.isEmpty()) { // empty problem must already be solved
            var solution = new StateSolution(new State[]{initialState});
            plans[0] = new Plan(problem, Result.ok(solution));
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

                var attemptSolutionLength = minSolutionLength;
                for(var j = 0; j < planCount; j++){
                    if(i == j) continue;
                    var solutionSize = attempts.get(j).getSolution().map(StateSolution::productiveSize).getOrElse(() -> 0);
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
}

