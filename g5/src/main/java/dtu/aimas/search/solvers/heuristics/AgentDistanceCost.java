package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class AgentDistanceCost implements Cost{
    public int calculate(State state, StateSpace space) {
        Problem problem = space.getProblem();
        return agentDistances(state, space, problem);
    }
    
    private int agentDistances(State state, StateSpace space, Problem problem) {
        int totalDistance = 0;
        for(var goal : problem.agentGoals) {
            var agent = space.getAgentByNumber(state, Character.getNumericValue(goal.label));
            totalDistance += problem.admissibleDist(agent.pos, goal.destination);
        }
        return totalDistance;
    }
}
