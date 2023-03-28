package dtu.aimas.search.solvers.blackboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
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
        for(var i = 0; i < plans.length; i++){
            var agent = initialState.agents.get(i);
            var subProblem = subProblemFor(fullProblem, agent);
            var solution = subSolverGenerator.apply(this.cost).solve(subProblem);
            plans[i] = new Plan(agent, subProblem, solution);
        }

        // Go through each agent solution, and check if it conflicts with another agent
        var map = Map.from(fullProblem);
        for(var plan: plans){
            plan.setConflicts(getConflicts(plan, plans, map));
        }

        var validSolutions = true;
        for(var plan: plans){
            if(plan.getSolution().isOk() && plan.getConflicts().isEmpty()) continue;
            validSolutions = false;
            break;
        }
        if(validSolutions) return Result.ok(mergeSolutions(plans));

        // Make a heuristic to avoid using resources in the other agents plan
        // Solve both conflicting agents again with this heuristic. Hopefully finding non conflicting solutions

        return Result.error(new NotImplemented());
    }

    private Solution mergeSolutions(Plan[] plans) {
        //TODO : Manage any solition types
        var actionSolutions = Arrays.stream(plans)
            .map(p -> (ActionSolution)p.getSolution().get())
            .toArray(ActionSolution[]::new);

        var agentCount = plans.length;
        var solutionLength = Arrays.stream(actionSolutions)
            .mapToInt(s -> s.size())
            .max()
            .orElse(0);

        var jointActions = new Action[solutionLength][agentCount];
        for(var step = 0; step < solutionLength; step++){
            for(var i = 0; i < agentCount; i++){
                var solution = actionSolutions[i];
                if(solution.size() <= step){
                    jointActions[step][i] = Action.NoOp;
                    continue;
                }

                var action = solution.getJointAction(step);
                assert action.length == 1;
                jointActions[step][i] = action[0];
            }
        }
        
        return new ActionSolution(jointActions);
    }


    private List<Solution> getConflicts(Plan plan, Plan[] plans, Map map) {
        // TODO maybe move to a conflict resolver for any generic solutions
        var conflicts = new ArrayList<Solution>();
        for(var other: plans){
            if (plan == other)continue;
            map.reset();
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
            if (map.isFree(box.pos)) {
                map.set(box.pos, box.label);
            }
            else {
                assert map.get(box.pos) == box.label: "Initial states of subproblems should not conflict";
            }
        }

        // Step through solutions and check if there are conflicts
        var maxSolutionLength = Math.max(mainSolution.size(), otherSolution.size());
        for(var step = 0; step < maxSolutionLength; step++){
            // If main solution has steps, check that the agents can perform the actions
            if(step < mainSolution.size()){
                var valid = validateJointAction(mainAgents, mainSolution.getJointAction(step), map);
                if(!valid) return Optional.of(mainSolution);
            }

            // If other solution has steps, check that the agents can perform the actions
            if(step < otherSolution.size()){
                var valid = validateJointAction(otherAgents, otherSolution.getJointAction(step), map);
                if(!valid) return Optional.of(otherSolution);
            }
        }

        // Apply actions and verify that no step conflicts
        for(var step = 0; step < maxSolutionLength; step++){
            // If main solution has steps, check that the agents can perform the actions
            if(step < mainSolution.size()){
                var jointAction = mainSolution.getJointAction(step);
                var valid = validateJointAction(mainAgents, jointAction, map);
                if(!valid) return Optional.of(mainSolution);
                applyJointAction(mainAgents, jointAction, map);
            }

            // If other solution has steps, check that the agents can perform the actions
            if(step < otherSolution.size()){
                var jointAction = otherSolution.getJointAction(step);
                var valid = validateJointAction(otherAgents, otherSolution.getJointAction(step), map);
                if(!valid) return Optional.of(otherSolution);
                applyJointAction(otherAgents, jointAction, map);
            }
        }

        return Optional.empty();
    }

    public void applyJointAction(Agent[] agents, Action[] jointAction, Map map){
        for(var i = 0; i < jointAction.length; i++){
            var agent = agents[i];
            var action = jointAction[i];
            applyAction(action, agent, map);
        }
    }

    public void applyAction(Action action, Agent agent, Map map){
        switch(action.type){
            case NoOp:
            {
                return;
            }

            case Move:
            {
                assert agent.label == map.get(agent.pos);
                map.clear(agent.pos);
                map.set(
                    agent.pos.row + action.agentRowDelta, 
                    agent.pos.col + action.agentColDelta,
                    agent.label);
                return;
            }

            case Pull:
            {
                assert agent.label == map.get(agent.pos);
                var boxRow = agent.pos.row - action.boxRowDelta;
                var boxCol = agent.pos.col - action.boxColDelta;
                var boxLabel = map.get(boxRow, boxCol);
                // TODO: Can/Should we check that it is the correct box?
                assert Box.isLabel(boxLabel);
            
                map.clear(boxRow, boxCol);
                map.set(agent.pos, boxLabel);
                map.set(agent.pos.row + action.agentRowDelta, agent.pos.col + action.agentColDelta, agent.label);
                return;
            }

            case Push:
                assert agent.label == map.get(agent.pos);
                var boxRow = agent.pos.row + action.agentRowDelta;
                var boxCol = agent.pos.col + action.agentColDelta;
                var boxLabel = map.get(boxRow, boxCol);
                assert Box.isLabel(boxLabel);
            
                map.clear(agent.pos);
                map.set(boxRow, boxCol, agent.label);
                map.set(agent.pos.row + action.agentRowDelta + action.boxRowDelta,
                    agent.pos.col + action.agentColDelta + action.boxColDelta,
                    boxLabel);

                return;
        }

    }

    public boolean validateJointAction(Agent[] agents, Action[] jointAction, Map map){
        for(var i = 0; i < jointAction.length; i++){
            var agent = agents[i];
            var action = jointAction[i];
            var valid = validateAction(action, agent, map);
            if(!valid) return false;
        }
        return true;
    }

    public boolean validateAction(Action action, Agent agent, Map map){
        // TODO: Potentially reuse validation logic from state space
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
