package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import java.util.Comparator;

public class GuidedDistanceSumCost implements Cost {
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

        for(var agent : problem.agents) {
            var colorBoxes = state.boxes.stream().filter(b -> b.color.equals(agent.color)).toList();
            if(colorBoxes.isEmpty()) continue;
            var shortestBoxGoalDist = Integer.MAX_VALUE;
            for(var box : colorBoxes) {
                var shortestDist = problem.boxGoals.stream()
                        .filter(g -> g.label == box.label)
                        .map(g -> problem.admissibleDist(g.destination, box.pos))
                        .min(Comparator.naturalOrder()).orElse(Integer.MAX_VALUE);
                if (shortestDist == Integer.MAX_VALUE || shortestDist == 0) continue;
                shortestDist += problem.admissibleDist(agent.pos, box.pos);
                if(shortestDist < shortestBoxGoalDist)
                    shortestBoxGoalDist = shortestDist;
            }
            if (shortestBoxGoalDist != Integer.MAX_VALUE) result += shortestBoxGoalDist;
        }

        return result;
    }
}
