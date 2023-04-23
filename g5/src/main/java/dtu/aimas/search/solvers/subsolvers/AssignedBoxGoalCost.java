package dtu.aimas.search.solvers.subsolvers;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.Cost;

import java.util.Collection;

public class AssignedBoxGoalCost implements Cost {
    private final Cost baseCost;
    private final Collection<RankedBox> rankings;

    public AssignedBoxGoalCost(Cost baseCost, Collection<RankedBox> rankings){
        this.baseCost = baseCost;
        this.rankings = rankings;
    }

    @Override
    public int calculate(State state, StateSpace space) {
        var problem = space.getProblem();
        assert problem.agents.size() == 1 : "Assigned box goal cost only available on single agent problems";
        var agent = state.agents.get(0);
        var result = baseCost.calculate(state, space);

        int distanceFromAgentToClosestBox = Integer.MAX_VALUE;
        var unfinishedBoxes = 0;
        for(var box: state.boxes){
            var assignment = rankings.stream().filter(r -> r.box().id == box.id).findAny();
            if(assignment.isEmpty()) continue; // box has no goal

            var goal = assignment.get().goal();
            var distanceToGoal = problem.admissibleDist(box.pos, goal.destination);
            if(distanceToGoal == 0) continue; // box is solved

            var distanceFromAgentToBox = problem.admissibleDist(agent.pos, box.pos);
            if(distanceFromAgentToBox < distanceFromAgentToClosestBox){
                distanceFromAgentToClosestBox = distanceFromAgentToBox;
            }

            unfinishedBoxes++;
            result += distanceToGoal;
        }

        // guide agent towards closest box
        result += distanceFromAgentToClosestBox;

        // buffer distance to unfinished boxes
        // to avoid sudden increase in cost when new box is selected
        result += unfinishedBoxes * problem.mapSize();

        return result;
    }
}
