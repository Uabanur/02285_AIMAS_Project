package dtu.aimas.search.solvers.blackboard;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.SolutionMerger;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.SolverMinLength;
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

        // Solve agent naively initially
        var planCount = Math.max(1, initialState.agents.size());
        var plans = new Plan[planCount];
        if(initialState.agents.isEmpty()){
            IO.debug("No agents found. Initial state solution attempt with no actions.");
            var solution = new StateSolution(new State[]{initialState});
            plans[0] = new Plan(null, fullProblem, Result.ok(solution));
        } else {
            IO.debug("Found %d agents. Solving sub problems independently.", initialState.agents.size());
            var startTime = System.currentTimeMillis();
            for(var i = 0; i < plans.length; i++){
                var agent = initialState.agents.get(i);
                var subProblem = subProblemFor(fullProblem, agent);
                var solution = subSolverGenerator.apply(this.baseCost)
                        .solve(subProblem)
                        .map(s -> (StateSolution)s);
                plans[i] = new Plan(agent, subProblem, solution);
            }
            var endTime = System.currentTimeMillis();
            IO.debug("Average time per naive solution: %,.2f ms", (endTime-startTime)/(float)planCount);
        }

        IO.debug("Searching for conflict free solution permutations");

        // TODO: Change queue to BestFirst priority queue,
        //      sorting by least amount of conflicts in the attempt permutation (find speed),
        //      or combined solution size (best solution)

        var set = new HashSet<AttemptPermutation>();
        var queue = new PriorityQueue<AttemptPermutation>();
//        var queue = new ArrayDeque<AttemptPermutation>();
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

            if(validAttempts(attempts, space)){
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

    private boolean validAttempts(List<Attempt> attempts, StateSpace space){
        for(var attempt: attempts){
            if (attempt.getSolution() == null) return false;
            if (!attempt.getConflicts().isEmpty()) return false;
        }

        var mergedSolution = SolutionMerger.mergeAttempts(attempts);
        return validSolution(mergedSolution, space);
    }

    private boolean validSolution(StateSolution solution, StateSpace space){
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
            if(!space.isValid(state)) return false;

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


    private Problem subProblemFor(Problem source, Agent agent) {
        // TODO use problem.subProblemFor from cbs branch
        var agents = List.of(agent);
        var boxes = source.boxes.stream().filter(b -> 
            b.color == agent.color).collect(Collectors.toList());
        var goals = new char[source.goals.length][source.goals[0].length];

        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var symbol = source.goals[row][col];
                if (symbol == 0) continue;
                if (symbol == agent.label){
                    goals[row][col] = symbol;
                }  else
                if (boxes.stream().anyMatch(b -> b.label == symbol)) {
                    goals[row][col] = symbol;
                }
            }
        }

        // TODO no need to precompute again. 
        // Just pass from parent problem when creating sub problems assuming walls are static.
        return new Problem(agents, boxes, source.walls, goals).precompute();
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

