package dtu.aimas.search.solvers.conflictbasedsearch;

import java.util.Collection;
import java.util.List;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.Constraint;
import lombok.Getter;

public class ConstrainedProblem extends Problem {
    @Getter
    private Constraint constraint;

    public ConstrainedProblem(Collection<Agent> agentCollection, Collection<Box> boxCollection, 
        boolean[][] walls, char[][] goals, Constraint constraint) {
        super(agentCollection, boxCollection, walls, goals);
        this.constraint = constraint;
    }

    public ConstrainedProblem(Collection<Agent> agentCollection, Collection<Box> boxCollection,
        char[][] goals, ConstrainedProblem parent) {
        super(agentCollection, boxCollection, goals, parent);
        this.constraint = parent.constraint;
    }

    public static ConstrainedProblem from(Problem parentProblem, Constraint constraint){
        return new ConstrainedProblem(parentProblem.agents, parentProblem.boxes, parentProblem.walls, parentProblem.goals, constraint);
    }

    public boolean isFree(Position pos, Agent agent, int timeStep) {
        return super.isFree(pos, agent, timeStep) && !constraint.isReserved(agent, pos, timeStep);
    }

    public Problem copyWith(List<Agent> agents, List<Box> boxes, char[][] goals) {
        return new ConstrainedProblem(agents, boxes, goals, this);
    }

}