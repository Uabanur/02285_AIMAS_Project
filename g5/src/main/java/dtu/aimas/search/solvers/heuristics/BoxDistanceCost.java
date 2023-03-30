package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.common.Box;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
/*
 * This cost is admissible because it will never overestimate that of an optimal solution
 * However, it is very simple so it might not actually decrease when getting closer to a solution sometimes
 * Could be seen as the cost of a relaxed problem where a box can go to multiple goals.
 */
public class BoxDistanceCost implements Cost {
    public int calculate(State state, StateSpace space) {
        Problem problem = space.getProblem();
        return simpleBoxDistances(state, space, problem);
    }

    private int simpleBoxDistances(State state, StateSpace space, Problem problem) {
        int totalDistance = 0;
        //for now adds distance between each goal and its closest box
        for(var goal : problem.boxGoals) {
            int distToClosest = Integer.MAX_VALUE;
            for(Box box : state.boxes) {
                if(box.label == goal.label) {
                    int dist = problem.admissibleDist(box.pos, goal.destination);
                    if(dist < distToClosest) distToClosest = dist;
                }
            }
            totalDistance += distToClosest;
        }
        return totalDistance;
    }
}
