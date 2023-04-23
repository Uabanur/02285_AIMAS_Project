package dtu.aimas.search.solvers.subsolvers;

import dtu.aimas.common.Agent;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.Cost;

public class PendingAgentCost implements Cost {
    private final Cost taskCost;
    private final Agent agent;

    public PendingAgentCost(Cost taskCost, Agent agent){
        this.taskCost = taskCost;
        this.agent = agent;
    }

    @Override
    public int calculate(State state, StateSpace space) {
        var problem = space.getProblem();
        var remainingTaskCost = taskCost.calculate(state, space);
        if(remainingTaskCost == 0) return agentCost(problem);

        return remainingTaskCost + problem.mapSize();
    }

    private int agentCost(Problem problem) {
        return problem.agentGoals.stream()
                .filter(g -> g.label == agent.label)
                .map(g -> problem.admissibleDist(agent.pos, g.destination))
                .findAny()
                .orElse(0);
    }
}
