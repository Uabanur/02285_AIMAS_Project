package dtu.aimas.search.solvers.blackboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Result;
import dtu.aimas.errors.NotImplemented;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Action;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.ActionSolution;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.ConflictPenalizedCost;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.GoalCount;

public class BlackboardSolver implements Solver {

    /* Ideas: 
        Rank boxes in order they need to be solved.
        They could be ranked such that if their goal blocks another box, they are ranked lower
    */

    private Cost cost;
    private Function<Cost, Solver> subSolverGenerator;
    public BlackboardSolver(Function<Cost, Solver> subSolverGenerator){
        this.subSolverGenerator = subSolverGenerator;
        cost = new GoalCount(); // change to precomputed dist cost
    }


    public Result<Solution> solve(Problem problem) {
        return ProblemParser.parse(problem)
                .flatMap(space -> solve(space));
    }

    private Result<Solution> solve(StateSpace space) {
        var initialState = space.getInitialState();
        var fullProblem = space.getProblem();

        // Solve agent naively initially
        var plans = new Plan[initialState.agents.size()];
        for(var i = 0; i < plans.length; i++){
            var agent = initialState.agents.get(i);
            var subProblem = subProblemFor(fullProblem, agent);
            var solution = subSolverGenerator.apply(this.cost).solve(subProblem);
            plans[i] = new Plan(agent, subProblem, solution);
        }

        // Go through each agent solution, and check if it conflicts with another agent
        for(var plan: plans){
            plan.setConflicts(getConflicts(plan, plans, space));
        }

        if(validSolutions(plans)) 
            return Result.ok(mergeSolutions(plans));

        // Make a heuristic to avoid using resources in the other agents plan
        // Solve both conflicting agents again with this heuristic. Hopefully finding non conflicting solutions
        for(var i = 0; i < plans.length; i++) {
            if(plans[i].getConflicts().isEmpty()) continue;
            var solution = subSolverGenerator
                    .apply(new ConflictPenalizedCost(cost, plans[i]))
                    .solve(plans[i].getProblem());
            plans[i] = new Plan(plans[i].getAgent(), plans[i].getProblem(), solution);
        }

        for(var plan: plans){
            plan.setConflicts(getConflicts(plan, plans, space));
        }

        if(validSolutions(plans)) 
            return Result.ok(mergeSolutions(plans));


        return Result.error(new NotImplemented());
    }

    private boolean validSolutions(Plan[] plans){
        for(var plan: plans){
            if (plan.getSolution().isError()) return false;
            if (!plan.getConflicts().isEmpty()) return false;
        }
        return true;
    }

    private Solution mergeSolutions(Plan[] plans) {
        //TODO : Manage any solition types
        var solutions = Arrays.stream(plans)
            .map(p -> (StateSolution)p.getSolution().get())
            .toArray(StateSolution[]::new);

        var agentCount = plans.length;
        var solutionLength = Arrays.stream(solutions)
            .mapToInt(s -> s.size())
            .max()
            .orElse(0);

        var jointActions = new Action[solutionLength][agentCount];
        for(var step = 1; step < solutionLength; step++){
            for(var i = 0; i < agentCount; i++){
                var solution = solutions[i];
                if(solution.size() <= step){
                    jointActions[step][i] = Action.NoOp;
                    continue;
                }

                var action = solution.getState(step).jointAction;
                assert action.length == 1;
                jointActions[step][i] = action[0];
            }
        }
        
        return new ActionSolution(jointActions);
    }


    private List<Solution> getConflicts(Plan plan, Plan[] plans, StateSpace space) {
        // TODO maybe move to a conflict resolver for any generic solutions
        var conflicts = new ArrayList<Solution>();
        for(var other: plans){
            if (plan == other)continue;
            var conflict = getConflict(plan, other, space);
            if(conflict.isEmpty()) continue;
            conflicts.add(conflict.get());
        }
        return conflicts;
    }


    private Optional<Solution> getConflict(Plan plan, Plan other, StateSpace space) {
        if(plan.getSolution().isError() || other.getSolution().isError())
            return Optional.empty();

        var mainSolution = (StateSolution)plan.getSolution().get();
        var otherSolution = (StateSolution)other.getSolution().get();
        var maxSolutionLength = Math.max(mainSolution.size(), otherSolution.size());
        for(var step = 1; step < maxSolutionLength; step++){
            var mainStep = Math.min(mainSolution.size()-1, step);
            var mainState = mainSolution.getState(mainStep);
            var otherStep = Math.min(otherSolution.size()-1, step);
            var otherState = otherSolution.getState(otherStep);
            
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
}
