package dtu.aimas.search.solvers.conflictbasedsearch;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Position;
import lombok.Getter;
import lombok.Setter;

public class Conflict {
    @Getter @Setter
    private Position position;

    @Getter @Setter
    private int timeStep;

    @Getter @Setter
    private Agent[] involvedAgents;
}
