package dtu.aimas.search.solvers.blackboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Map;
import dtu.aimas.common.Result;
import dtu.aimas.errors.NotImplemented;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Action;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.ActionSolution;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
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
        var naiveSolver = subSolverGenerator.apply(this.cost);
        for(var i = 0; i < plans.length; i++){
            var agent = initialState.agents.get(i);
            var subProblem = subProblemFor(fullProblem, agent);
            var solution = naiveSolver.solve(subProblem);
            plans[i] = new Plan(agent, subProblem, solution);
        }

        // Go through each agent solution, and check if it conflicts with another agent
        for(var plan: plans){
            plan.setConflicts(getConflicts(plan, plans, Map.from(fullProblem)));
        }

        // Make a heuristic to avoid using resources in the other agents plan
        // Solve both conflicting agents again with this heuristic. Hopefully finding non conflicting solutions

        return Result.error(new NotImplemented());
    }

    private List<Solution> getConflicts(Plan plan, Plan[] plans, Map map) {
        // TODO maybe move to a conflict resolver for any generic solutions
        var conflicts = new ArrayList<Solution>();
        for(var other: plans){
            if (plan == other)continue;
            var conflict = getConflict(plan, other, map);
            if(conflict.isEmpty()) continue;
            conflicts.add(conflict.get());
        }
        return conflicts;
    }


    private Optional<Solution> getConflict(Plan plan, Plan other, Map map) {
        if(plan.getSolution().isError() || other.getSolution().isError())
            return Optional.empty();

        // Add main problem agents to the map
        var mainProblem = plan.getProblem();
        var mainAgents = mainProblem.agents.stream().map(a -> a.clone()).toArray(Agent[]::new);
        var mainSolution = (ActionSolution)plan.getSolution().get();
        for(var agent: mainProblem.agents){
            assert map.isFree(agent.pos) : "Agent positions should not conflict";
            map.set(agent.pos, agent.label);
        }

        // Add main problem boxes to the map
        for(var box: mainProblem.boxes){
            assert map.isFree(box.pos)  : "Agent and box positions should not conflict";
            map.set(box.pos, box.label);
        }


        // Add other problem agents to the map
        var otherProblem = other.getProblem();
        var otherAgents = otherProblem.agents.stream().map(a -> a.clone()).toArray(Agent[]::new);
        var otherSolution = (ActionSolution)other.getSolution().get();
        for(var agent: otherProblem.agents){
            assert map.isFree(agent.pos) : "Initial states of subproblems should not conflict";
            map.set(agent.pos, agent.label);
        }

        // Add other problem boxes to the map
        for(var box: otherProblem.boxes){
            assert map.isFree(box.pos) : "Initial states of subproblems should not conflict";
            map.set(box.pos, box.label);
        }

        // Step through solutions and check if there are conflicts
        var maxSolutionLength = Math.max(mainSolution.size(), otherSolution.size());
        for(var step = 0; step < maxSolutionLength; step++){

            // If main solution has steps, check that the agents can perform the actions
            if(step < mainSolution.size()){
                var mainActions = mainSolution.getJointAction(step);
                for(var i = 0; i < mainActions.length; i++){
                    var agent = mainAgents[i];
                    var action = mainActions[i];
                    assert map.isFree(agent.pos);
                    var valid = validateAction(action, agent, map);
                    if(!valid) return Optional.of(otherSolution);
                }
            }

            // If other solution has steps, check that the agents can perform the actions
            if(step < otherSolution.size()){
                var otherActions = otherSolution.getJointAction(step);
                for(var i = 0; i < otherActions.length; i++){
                    var agent = otherAgents[i];
                    var action = otherActions[i];
                    assert map.isFree(agent.pos);
                    var valid = validateAction(action, agent, map);
                    if(!valid) return Optional.of(otherSolution);
                }
            }
        }

        return Optional.empty();
    }

    public boolean validateAction(Action action, Agent agent, Map map){
        switch(action.type){
            case NoOp:
                return true;

            case Move:
                return map.isFree(
                    agent.pos.row + action.agentRowDelta, 
                    agent.pos.col + action.agentColDelta);

            case Pull:
                return map.isFree(
                    agent.pos.row + action.agentRowDelta, 
                    agent.pos.col + action.agentColDelta);

            case Push:
                return map.isFree(
                    agent.pos.row + action.agentRowDelta + action.boxRowDelta, 
                    agent.pos.col + action.agentColDelta + action.boxColDelta);
        }

        return true;
    }


    private Problem subProblemFor(Problem source, Agent agent) {
        // TODO use problem.subProblemFor from cbs branch
        var agents = List.of(agent);
        var boxes = source.boxes.stream().filter(b -> 
            b.color == agent.color).collect(Collectors.toList());
        var goals = source.goals.clone();

        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var symbol = goals[row][col];
                if (symbol == 0) continue;
                if (symbol == agent.label) continue;
                if (boxes.stream().anyMatch(b -> b.label == symbol)) continue;
                goals[row][col] = 0;
            }
        }

        // TODO no need to precompute again. 
        // Just pass from parent problem when creating subproblems assuming walls are static.
        return new Problem(agents, boxes, source.walls, goals).precompute();
    }
}
