package dtu.aimas.search.solvers.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import dtu.aimas.common.Box;
import dtu.aimas.common.Position;
import dtu.aimas.communication.IO;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class SingleGoalDistanceCost implements Cost {
    @Override
    public int calculate(State state, StateSpace space) {
        var problem = space.problem();
        var result = 0;

        for(var goal : problem.boxGoals){
            if(state.boxes.stream().anyMatch(b -> b.label == goal.label && b.pos.equals(goal.destination)))
                continue;
            var compatibleBoxes = state.boxes.stream().filter(b -> b.label == goal.label).toList();
            var closestBox = compatibleBoxes.get(0);
            var minDist = Integer.MAX_VALUE;
            for(Box box : compatibleBoxes) {
                var dist = problem.admissibleDist(box.pos, goal.destination);
                if(dist < minDist) {
                    closestBox = box;
                    minDist = dist;
                }
            }
            result += minDist;
            //distance to box being solved
            if(minDist > 0) {
                Box b = closestBox;
                var agent = state.agents.stream().filter(a -> a.color == b.color).findFirst().get();
                result += problem.admissibleDist(agent.pos, closestBox.pos)-1;
            }
            
        }
        //if(result < 2)IO.info(result);
        //if(result > 0) result += 10; //penalty to keep agent from going to his goal

        for(var goal: problem.agentGoals){
            var agent = state.agents.stream().filter(a -> a.label == goal.label).findAny();
            if(agent.isEmpty()) throw new IllegalStateException("Unsatisfiable goal");
            result += problem.admissibleDist(agent.get().pos, goal.destination);
        }

        return result;
    }
}
