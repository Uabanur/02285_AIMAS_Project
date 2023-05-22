package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import java.util.Comparator;
import java.util.HashSet;

public class TestCost2 implements Cost{
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
        var assignedBoxes = new HashSet<Box>();
        var unfinishedBoxes = new HashSet<Box>();

        for(var boxGoal: problem.boxGoals){
            var boxResult = agentBoxes.stream()
                    .filter(b -> b.label == boxGoal.label)
                    .filter(b -> !assignedBoxes.contains(b))
                    .min(Comparator.comparingInt(b -> problem.admissibleDist(b.pos, boxGoal.destination)));

            if(boxResult.isEmpty()) continue; // box goal belongs to a different agent.
            var box = boxResult.get();

            var boxCost = problem.admissibleDist(box.pos, boxGoal.destination);
            cost += boxCost;
            assignedBoxes.add(box);

            if(boxCost > 0){
                unfinishedBoxes.add(box);
            }
        }

        if(!unfinishedBoxes.isEmpty()){
            var closestUnfinishedBox = unfinishedBoxes.stream().min(Comparator.comparingInt(b -> problem.admissibleDist(agent.pos, b.pos)));
            cost += problem.admissibleDist(agent.pos, closestUnfinishedBox.get().pos);
            cost += (unfinishedBoxes.size()-1)*problem.mapSize();
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
