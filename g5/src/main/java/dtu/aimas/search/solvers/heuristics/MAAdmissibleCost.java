package dtu.aimas.search.solvers.heuristics;

import java.util.List;
import java.util.stream.Collectors;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Goal;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class MAAdmissibleCost implements Cost {
    //Not accurate if there are more boxgoals than agents
    public int calculate(State state, StateSpace space) {
        Problem problem = space.getProblem();
        var allBoxes = state.boxes;
        var allAgents = state.agents;
        // We want the cost of completing the longest goal in the shortest way possible
        // All other goals will (should) be completed while the longest one is completed, so cost is admissible
        int longestGoalCompletionDist = 0;
        for(Goal goal : problem.boxGoals) {
            // Find shortest way to complete goal (test the best box-agent combination to do so)
            int minGoalCompleteDist = Integer.MAX_VALUE;
            List<Box> boxes = allBoxes.stream().filter(b -> b.label == goal.label).collect(Collectors.toList());
            for(Box box : boxes) {
                // Find shortest way to get box to goal
                int minBoxGoalDist = Integer.MAX_VALUE;
                int boxGoalDist = problem.admissibleDist(box.pos, goal.destination);
                if(boxGoalDist == Integer.MAX_VALUE) continue;
                List<Agent> agents = allAgents.stream().filter(a -> a.color == box.color).collect(Collectors.toList());
                for(Agent agent : agents) {
                    int agentGoalDist = 0;
                    List<Goal> agentGoals = problem.agentGoals.stream().filter(agoal -> agoal.label == agent.label).collect(Collectors.toList());
                    // Agents don't have to have goals, but if they do, they only have one
                    if(!agentGoals.isEmpty()) {
                        agentGoalDist = problem.admissibleDist(goal.destination, agentGoals.get(0).destination);
                        if(agentGoalDist == Integer.MAX_VALUE) continue;
                    }
                    //the cost for agent to take box to goal is the distance between agent -> box -> goal + agent -> agentGoal
                    int agentBoxDist = problem.admissibleDist(agent.pos, box.pos);
                    if(agentBoxDist == Integer.MAX_VALUE) continue;
                    int dist =  agentBoxDist + boxGoalDist + agentGoalDist;
                    //if one of the costs is max value, then the sum will be negative.
                    if(dist < 0) continue;
                    if(dist < minBoxGoalDist) minBoxGoalDist = dist;
                }
                if(minBoxGoalDist < minGoalCompleteDist) minGoalCompleteDist = minBoxGoalDist;
            }
            if(minGoalCompleteDist > longestGoalCompletionDist) longestGoalCompletionDist = minGoalCompleteDist;
        }
        return longestGoalCompletionDist;
    }
}
