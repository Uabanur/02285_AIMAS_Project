package dtu.aimas.search.solvers.subsolvers;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Goal;
import dtu.aimas.search.Problem;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class BoxRanker {
    public static Collection<RankedBox> closestBox(Problem problem, Agent agent){
        var boxes = problem.boxes.stream().filter(b -> b.color == agent.color).toList();
        var goals = problem.boxGoals.stream().filter(g -> boxes.stream().anyMatch(b -> b.label == g.label)).toList();

        var ranked = new HashSet<RankedBox>();
        for(var goal: goals){
            var box = closestBox(goal, boxes, ranked, problem);
            ranked.add(new RankedBox(box, goal, 0));
        }

        return ranked;
    }

    private static Box closestBox(Goal goal, List<Box> boxes, HashSet<RankedBox> assigned, Problem problem){
        var options = boxes.stream().filter(b ->
                assigned.stream().noneMatch(a ->
                        a.box().equals(b))).toList();

        var closest = 0;
        var minDistance = problem.admissibleDist(options.get(closest).pos, goal.destination);
        for(var i = 1; i < options.size(); i++){
            var distance = problem.admissibleDist(options.get(i).pos, goal.destination);
            if(distance >= minDistance) continue;

            closest = i;
            minDistance = distance;
        }

        return options.get(closest);
    }
}
