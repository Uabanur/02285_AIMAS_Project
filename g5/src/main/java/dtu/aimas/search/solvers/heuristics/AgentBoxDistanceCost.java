package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class AgentBoxDistanceCost implements Cost{
    public int calculate(State state, StateSpace space) {
        Problem problem = space.getProblem();
        return agentBoxDistances(state, space, problem);
    }

    private int agentBoxDistances(State state, StateSpace space, Problem problem) {
        int totalDistance = 0;
        for(Agent agent : state.agents) {
            int minDist = Integer.MAX_VALUE;
            for(Box box : state.boxes) {
                if(box.color == agent.color){
                    int distance = problem.admissibleDist(agent.pos, box.pos);
                    if(distance < minDist) minDist = distance;
                }
            }
            totalDistance += minDist;
        }
        return totalDistance;
    }
}
