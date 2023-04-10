package dtu.aimas.search.solvers.blackboard;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Result;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.errors.UnreachableState;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Action;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.ConflictPenalizedCost;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.GoalCount;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BlackboardSolver implements Solver {

    /* Ideas: 
        Rank boxes in order they need to be solved.
        They could be ranked such that if their goal blocks another box, they are ranked lower
    */

    private final Cost cost;
    private final Function<Cost, Solver> subSolverGenerator;
    public BlackboardSolver(Function<Cost, Solver> subSolverGenerator){
        this.subSolverGenerator = subSolverGenerator;
        cost = new GoalCount(); // change to precomputed dist cost
    }


    public Result<Solution> solve(Problem problem) {
        return ProblemParser.parse(problem)
                .flatMap(this::solve);
    }

    private Result<Solution> solve(StateSpace space) {
        var initialState = space.getInitialState();
        var fullProblem = space.getProblem();

        // Solve agent naively initially
        var planCount = Math.max(1, initialState.agents.size());
        var plans = new Plan[planCount];
        if(initialState.agents.isEmpty()){
            var solution = new StateSolution(new State[]{initialState});
            plans[0] = new Plan(null, fullProblem, Result.ok(solution));
        } else {
            for(var i = 0; i < plans.length; i++){
                var agent = initialState.agents.get(i);
                var subProblem = subProblemFor(fullProblem, agent);
                var solution = subSolverGenerator.apply(this.cost)
                        .solve(subProblem)
                        .map(s -> (StateSolution)s);
                plans[i] = new Plan(agent, subProblem, solution);
            }
        }

        var set = new HashSet<AttemptPermutation>();
        var queue = new ArrayDeque<AttemptPermutation>();
        queue.add(new AttemptPermutation(new int[planCount]));

        while(true){

            // Verify that we can continue
            if(queue.isEmpty())
                return Result.error(new SolutionNotFound("No more attempt permutations"));

            // Check if goal is found
            var attemptPermutation = queue.poll();
            var attempts = attemptPermutation.getAttempts(plans);

            for(var attempt: attempts)
                attempt.setConflicts(getConflicts(attempt, attempts, space));

            if(validAttempts(attempts, space))
                return Result.ok(mergeAttempts(attempts));

            // Calculate all neighbor attempts
            for(var i = 0; i < planCount; i++) {
                if(attempts.get(i).getConflicts().isEmpty()) continue;
                var solution = subSolverGenerator
                        .apply(new ConflictPenalizedCost(cost, attempts.get(i)))
                        .solve(plans[i].getProblem())
                        .map(s -> (StateSolution)s);
                plans[i].addAttempt(new Attempt(solution));

                var options = Arrays.stream(plans).mapToInt(p -> p.getAttempts().size()).toArray();
                for(var permutation: combinations(options, i, plans[i].lastAttemptIndex())){
                    var next = new AttemptPermutation(permutation);
                    if(set.contains(next)) continue;
                    queue.add(next);
                    set.add(next);
                }
            }
        }
//
//        var attempts = IntStream.range(0, planCount)
//                .mapToObj(i -> plans[i].getAttempts().get(attemptIndices[i])).collect(Collectors.toList());
//
//        // Go through each agent solution, and check if it conflicts with another agent
//        // TODO: this may be redundant now. We could just check if solutions are valid
//        for(var attempt: attempts){
//            attempt.setConflicts(getConflicts(attempt, attempts, space));
//        }
//
////        if(validSolutions(plans, space))
////            return Result.ok(mergeSolutions(plans));
//        if(validAttempts(attempts, space))
//            return Result.ok(mergeAttempts(attempts));
//
//        // Make a heuristic to avoid using resources in the other agents plan
//        // Solve both conflicting agents again with this heuristic. Hopefully finding non conflicting solutions
//        for(var i = 0; i < attempts.size(); i++) {
//            if(attempts.get(i).getConflicts().isEmpty()) continue;
//            var solution = subSolverGenerator
//                    .apply(new ConflictPenalizedCost(cost, attempts.get(i)))
//                    .solve(plans[i].getProblem())
//                    .map(s -> (StateSolution)s);
//            plans[i].addAttempt(new Attempt(solution));
//            attemptIndices[i] += 1;
//        }
//
//        attempts = IntStream.range(0, planCount)
//                .mapToObj(i -> plans[i].getAttempts().get(attemptIndices[i])).collect(Collectors.toList());
//
//        for(var attempt: attempts){
//            attempt.setConflicts(getConflicts(attempt, attempts, space));
//        }
//
//        if(validAttempts(attempts, space))
//            return Result.ok(mergeAttempts(attempts));
//
//        return Result.error(new NotImplemented());
    }

    public StateSolution mergeAttempts(List<Attempt> attempts){
        var solutions = attempts.stream()
                .map(a -> a.getSolution().get())
                .toList();
        return mergeSolutions(solutions);
    }

    public StateSolution mergeSolutions(List<StateSolution> solutions){
        var solutionLength = solutions.stream()
            .mapToInt(StateSolution::size)
            .max()
            .orElse(0);

        var states = new State[solutionLength];
        for(var step = 0; step < solutionLength; step++){
            var parent = step == 0 ? null : states[step-1];
            states[step] = combinedState(solutions, step, parent);
        }

        return new StateSolution(states);
    }

    private State combinedState(List<StateSolution> solutions, int step, State parent){
        var agents = solutions.stream()
            .flatMap(s -> s.getState(Math.min(s.size()-1, step)).agents.stream())
            .collect(Collectors.toCollection(ArrayList::new));

        var boxes = solutions.stream()
            .flatMap(s -> s.getState(Math.min(s.size()-1,step)).boxes.stream())
            .collect(Collectors.toCollection(ArrayList::new));

        // Sort agents and joint actions the same
        var jointAction = new Action[agents.size()];
        var agentArray = new Agent[agents.size()];
        var filled = 0;
        for(var solution: solutions){
            var solutionFinished = step >= solution.size();
            var state = solution.getState(Math.min(solution.size()-1, step));
            for(var i = 0; i < state.agents.size(); i++){
                agentArray[filled + i] = state.agents.get(i);
                jointAction[filled + i] = step == 0 || solutionFinished ? Action.NoOp : state.jointAction[i];
            }
            filled += state.agents.size();
        }

        var indices = IntStream.range(0, agents.size()).boxed().toArray(Integer[]::new);
        Arrays.sort(indices, new AgentIndexComparator(agentArray));
        var sortedJointAction = new Action[indices.length];
        var sortedAgents = new Agent[indices.length];
        for(var i = 0; i < indices.length; i++){
            sortedJointAction[i] = jointAction[indices[i]];
            sortedAgents[i] = agentArray[indices[i]];
        }

        var sortedAgentsList = Stream.of(sortedAgents).collect(Collectors.toCollection(ArrayList::new));
        if (step == 0) return new State(sortedAgentsList, boxes);
        return new State(parent, sortedAgentsList, boxes, sortedJointAction);
    }

    static class AgentIndexComparator implements Comparator<Integer>
    {
        private final Agent[] agents;
        AgentIndexComparator(Agent[] agents) { this.agents = agents; }

        @Override
        public int compare(Integer first, Integer second) {
            return Character.compare(this.agents[first].label, this.agents[second].label);
        }
    }

//    private boolean validSolutions(Plan[] plans, StateSpace space){
//        for(var plan: plans){
//            if (plan.getSolution().isError()) return false;
//            if (!plan.getConflicts().isEmpty()) return false;
//        }
//
//        var solutions = Stream.of(plans).map(p -> (StateSolution)p.getSolution().get())
//                            .toArray(StateSolution[]::new);
//        var mergedSolution = mergeSolutions(solutions);
//        return validSolution(mergedSolution, space);
//    }

    private boolean validAttempts(List<Attempt> attempts, StateSpace space){
        for(var attempt: attempts){
            if (attempt.getSolution() == null) return false;
            if (!attempt.getConflicts().isEmpty()) return false;
        }

        var mergedSolution = mergeAttempts(attempts);
        return validSolution(mergedSolution, space);
    }

    private boolean validSolution(StateSolution solution, StateSpace space){
        var expectedInitialState = space.getInitialState();
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

//    private Solution mergeSolutions(Plan[] plans) {
//        //TODO : Manage any solution types
//        var solutions = Arrays.stream(plans)
//            .map(p -> (StateSolution)p.getSolution().get())
//            .toArray(StateSolution[]::new);
//
//        var agentCount = plans.length;
//        var solutionLength = Arrays.stream(solutions)
//            .mapToInt(s -> s.size())
//            .max()
//            .orElse(0);
//
//        var jointActions = new Action[solutionLength][agentCount];
//        for(var step = 1; step < solutionLength; step++){
//            for(var i = 0; i < agentCount; i++){
//                var solution = solutions[i];
//                if(solution.size() <= step){
//                    jointActions[step][i] = Action.NoOp;
//                    continue;
//                }
//
//                var action = solution.getState(step).jointAction;
//                assert action.length == 1;
//                jointActions[step][i] = action[0];
//            }
//        }
//
//        return new ActionSolution(jointActions);
//    }


    private List<StateSolution> getConflicts(Attempt attempt, List<Attempt> attempts, StateSpace space) {
        // TODO maybe move to a conflict resolver for any generic solutions
        var conflicts = new ArrayList<StateSolution>();
        for(var other: attempts){
            if (attempt == other)continue;
            var conflict = getConflict(attempt, other, space);
            if(conflict.isEmpty()) continue;
            conflicts.add(conflict.get());
        }
        return conflicts;
    }


    private Optional<StateSolution> getConflict(Attempt attempt, Attempt other, StateSpace space) {
        if(attempt.getSolution().isError() || other.getSolution().isError())
            return Optional.empty();

        var mainSolution = attempt.getSolution().get();
        var otherSolution = other.getSolution().get();
        var maxSolutionLength = Math.max(mainSolution.size(), otherSolution.size());
        for(var step = 1; step < maxSolutionLength; step++){
            var mainStep = Math.min(mainSolution.size()-1, step);
            var mainState = mainSolution.getState(mainStep);
            var otherStep = Math.min(otherSolution.size()-1, step);
            var otherState = otherSolution.getState(otherStep);

            if(mainState.parent == null || otherState.parent == null) throw new UnreachableState();

            var prevMainState = step < mainSolution.size() ? mainState.parent : mainState;
            var prevOtherState = step < otherSolution.size() ? otherState.parent : otherState;

            var previousState = combineState(prevMainState, prevOtherState);
            if(!space.isValid(previousState)) return Optional.of(otherSolution);
            
            if(step < mainSolution.size()){ // If main solution is finished its noop'ing
                for(var i = 0; i < mainState.agents.size(); i++){
                    if(space.isApplicable(previousState, mainState.parent.agents.get(i), mainState.jointAction[i])) continue;
                    return Optional.of(otherSolution);
                }
            }

            if(step < otherSolution.size()){ // If other solution is finished its noop'ing
                for(var i = 0; i < otherState.agents.size(); i++){
                    if(space.isApplicable(previousState, otherState.parent.agents.get(i), otherState.jointAction[i])) continue;
                    return Optional.of(otherSolution);
                }
            }
        }

        return Optional.empty();
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
                    continue;
                } 

                if (boxes.stream().anyMatch(b -> b.label == symbol)) {
                    goals[row][col] = symbol;
                    continue;
                }
            }
        }

        // TODO no need to precompute again. 
        // Just pass from parent problem when creating subproblems assuming walls are static.
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
