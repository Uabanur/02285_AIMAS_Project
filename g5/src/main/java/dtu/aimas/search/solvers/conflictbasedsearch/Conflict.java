package dtu.aimas.search.solvers.conflictbasedsearch;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Position;
import lombok.Getter;

public class Conflict {
    @Getter
    private Position position;
    @Getter
    private int timeStep;
    @Getter
    private Agent[] involvedAgents;

    public Conflict(Position position, int timeStep, Agent[] involvedAgents) {
        this.position = position;
        this.timeStep = timeStep;
        this.involvedAgents = involvedAgents;
    }

    @Override
    public String toString() {
        return String.format("Position: %d,%d. TimeStep: %d. Num of agents: %d.", this.position.row, this.position.col, timeStep, involvedAgents.length);
    }
}
