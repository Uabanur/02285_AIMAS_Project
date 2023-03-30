package dtu.aimas.search.solvers.heuristics;

import java.util.HashSet;
import java.util.Set;

import dtu.aimas.common.Box;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

/*
 * This cost is not admissible because it can overestimate the cost of an optimal solution
 * However, the cost it returns is the correct cost for some solution 
 * (the solution depends on ordering of boxes and goals in the problem)
 */
public class NonAdmissibleBoxDistCost implements Cost {
    public int calculate(State state, StateSpace space) {
        Problem problem = space.getProblem();
        return simpleBoxDistances(state, space, problem);
    }

    private int simpleBoxDistances(State state, StateSpace space, Problem problem) {
        int totalDistance = 0;
        Set<Box> usedBox = new HashSet<>();
        //for now adds distance between each goal and its closest not yet assigned box
        for(var goal : problem.boxGoals) {
            int distToClosest = Integer.MAX_VALUE;
            Box closestBox = state.boxes.get(0);
            for(Box box : state.boxes) {
                if(!usedBox.contains(box) && box.label == goal.label) {
                    int dist = problem.admissibleDist(box.pos, goal.destination);
                    if(dist < distToClosest) {
                        distToClosest = dist;
                        closestBox = box;
                        if(distToClosest == 0) break;
                    }
                }
            }
            usedBox.add(closestBox);
            totalDistance += distToClosest;
        }
        return totalDistance;
    }
}
