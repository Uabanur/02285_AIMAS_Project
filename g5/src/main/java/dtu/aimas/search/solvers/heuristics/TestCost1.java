package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.common.Agent;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class TestCost1 implements Cost{
    @Override
    public int calculate(State state, StateSpace space) {
        var problem = space.getProblem();
        var cost = 0;
        for(var agent: state.agents){
            cost += agentCost(agent, state, problem);
        }
        return cost;
    }

    private int agentCost(Agent agent, State state, Problem problem) {
        var cost = 0;
        var agentBoxes = state.boxes.stream().filter(b -> b.color == agent.color).toList();
        for(var boxGoal: problem.boxGoals){
            cost += agentBoxes.stream()
                    .filter(b -> b.label == boxGoal.label)
                    .mapToInt(b -> problem.admissibleDist(b.pos, boxGoal.destination))
                    .min()
                    .orElse(0);
        }

        var agentGoal = problem.agentGoals.stream().filter(g -> g.label == agent.label).findAny();
        if(agentGoal.isEmpty()) return cost;

        if(cost != 0){
            cost += problem.mapSize();
        } else {
            cost += problem.admissibleDist(agent.pos, agentGoal.get().destination);
        }

        return cost;
    }
}
