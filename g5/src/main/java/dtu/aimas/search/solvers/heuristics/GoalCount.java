package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class GoalCount implements Cost {

    public int calculate(State state, StateSpace space) {
        var problem = space.problem();
        var goals = problem.agentGoals.size() + problem.boxGoals.size();
        var satisfiedGoals = space.getSatisfiedAgentGoalsCount(state) + space.getSatisfiedBoxGoalsCount(state);

        return goals - satisfiedGoals;
    }
}
