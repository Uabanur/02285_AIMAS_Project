package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class GoalCount implements Cost {

    public int calculate(State state, StateSpace space) {
        return space.getSatisfiedAgentGoalsCount(state) + space.getSatisfiedBoxGoalsCount(state);
    }

}
