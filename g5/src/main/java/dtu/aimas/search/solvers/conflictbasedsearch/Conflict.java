package dtu.aimas.search.solvers.conflictbasedsearch;

import java.util.ArrayList;
import java.util.HashSet;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Position;
import lombok.Getter;

public class Conflict {
    @Getter
    private Position position;
    @Getter
    private int timeStep;
    @Getter
    private HashSet<Agent> involvedAgents;

    public Conflict(Position position, int timeStep, HashSet<Agent> involvedAgents) {
        this.position = position;
        this.timeStep = timeStep;
        this.involvedAgents = involvedAgents;
    }

    public Conflict(Position position, int timeStep){
        this.position = position;
        this.timeStep = timeStep;
        this.involvedAgents = new HashSet<Agent>();
    }

    public void involveAgent(Agent agent){
        this.involvedAgents.add(agent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Agent agent : involvedAgents) {
            sb.append(agent.label);
            sb.append(", ");
        }
        return String.format("Position: %d,%d. TimeStep: %d. Num of agents: %d. Involved agents: %s", this.position.row, this.position.col, timeStep, involvedAgents.size(), sb.toString());
    }
}
