package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class DistanceSumCost implements Cost {
    @Override
    public int calculate(State state, StateSpace space) {
        var problem = space.problem();
        var result = 0;

        for(var goal: problem.agentGoals){
            var agent = state.agents.stream().filter(a -> a.label == goal.label).findAny();
            if(agent.isEmpty()) throw new IllegalStateException("Unsatisfiable goal");
            result += problem.admissibleDist(agent.get().pos, goal.destination);
        }

        for(var goal : problem.boxGoals){
            var minGoalDistance = state.boxes.stream()
                    .filter(b -> b.label == goal.label)
                    .map(b -> problem.admissibleDist(b.pos, goal.destination))
                    .min(Integer::compareTo)
                    .orElse(0);

            result += minGoalDistance;
        }

        return result;
    }
}
