package dtu.aimas.search.problems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Goal;
import dtu.aimas.search.Problem;

public class AgentBoxAssignationSplitter implements ProblemSplitter {
    private HashMap<Agent, List<Goal>> agentAssignedGoals;
    private HashMap<Agent, List<Box>> agentAssignedBoxes;
    private Set<Box> unnassignedBoxes;
    private Problem problem; 
    private ArrayList<Goal> orderedBoxGoals;
    
    @Override
    public List<Problem> split(Problem problem) {
        this.problem = problem;
        agentAssignedGoals = new HashMap<>();
        agentAssignedBoxes = new HashMap<>();
        unnassignedBoxes = new HashSet<>(problem.boxes);
        orderGoalsByPriority();
        assignGoals();
        return problem.agents.stream().map(a -> subProblemForAgent(a)).toList();
    }

    private void assignGoals() {
        HashMap<Agent, Integer> agentCost = new HashMap<>();

        for(Goal goal : orderedBoxGoals) {
            List<Box> compatibleBoxes = unnassignedBoxes.stream().filter(
                b -> b.label == goal.label 
                && problem.agents.stream().anyMatch(a -> a.color.equals(b.color))
                ).collect(Collectors.toList());
            if(compatibleBoxes.isEmpty()) continue; //Goal isn't solvable!

            Box closestBox = compatibleBoxes.get(0);
            int closestBoxDist = Integer.MAX_VALUE;
            for(Box box : compatibleBoxes) {
                int dist = problem.admissibleDist(box.pos, goal.destination);
                if(dist < closestBoxDist) {
                    closestBox = box;
                    closestBoxDist = dist;
                }
            }
            Box theBox = closestBox;
            List<Agent> compatibleAgents = problem.agents.stream().filter(
                a -> a.color == theBox.color
            ).collect(Collectors.toList());
            if(compatibleAgents.isEmpty()) continue;
            Agent closestAgent = compatibleAgents.get(0);

            int closestAgentDist = Integer.MAX_VALUE;
            for(Agent agent : compatibleAgents) {
                if(!agentCost.containsKey(agent)) agentCost.put(agent, 0);
                int dist = problem.admissibleDist(agent.pos, closestBox.pos) + agentCost.get(agent);
                if(dist < 0) dist = Integer.MAX_VALUE;
                if(dist < closestAgentDist) {
                    closestAgent = agent;
                    closestAgentDist = dist;
                }
            }

            if(!agentAssignedGoals.containsKey(closestAgent)) {
                agentAssignedGoals.put(closestAgent, new ArrayList<>());
                agentAssignedBoxes.put(closestAgent, new ArrayList<>());
            }
            agentAssignedGoals.get(closestAgent).add(goal);
            agentAssignedBoxes.get(closestAgent).add(closestBox);
            unnassignedBoxes.remove(closestBox);
            //2 times because it has to come back for calculations to be correct
            agentCost.put(closestAgent, agentCost.get(closestAgent)+ (closestBoxDist + closestAgentDist)*2); 
        }        

    }

    public Problem subProblemForAgent(Agent agent) {
        var agents = List.of(agent);
        var goals = new char[problem.goals.length][problem.goals[0].length];
        List<Box> boxes;
        if(agentAssignedGoals.containsKey(agent)) {
            agentAssignedGoals.get(agent).stream().forEach(g -> goals[g.destination.row][g.destination.col] = g.label);
            boxes = agentAssignedBoxes.get(agent);
            boxes.addAll(unnassignedBoxes.stream().filter(ub -> ub.color.equals(agent.color)).toList());
        }
        else boxes = new ArrayList<>();
        var agentGoal = problem.agentGoals.stream().filter(ag -> ag.label == agent.label).findAny();
        if(agentGoal.isPresent()) {
            var goal = agentGoal.get();
            goals[goal.destination.row][goal.destination.col] = goal.label;
        }
//        return new Problem(agents, boxes, goals, problem);
        return problem.copyWith(agents, boxes, goals);
    }

    private void orderGoalsByPriority() {
        orderedBoxGoals = new ArrayList<Goal>();
        //We want goals in dead-ends to be solved first and those in chokepoints last
        for(Goal goal : problem.boxGoals) {
            if(problem.isDeadEnd(goal.destination)) {
                orderedBoxGoals.add(0, goal);
            }
            else if(problem.isChokepoint(goal.destination)) {
                orderedBoxGoals.add(Math.max(0,orderedBoxGoals.size()-1), goal);
            }
            else {
                int insertPosition = Math.ceilDiv((orderedBoxGoals.size()-1),2);
                orderedBoxGoals.add(Math.max(0,insertPosition), goal);
            }
        }
    }
}

