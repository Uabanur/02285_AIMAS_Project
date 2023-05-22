package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import dtu.aimas.common.*;
import dtu.aimas.search.Action;
import lombok.Getter;
import lombok.NonNull;

public class State {
    public final State parent;
    public final ArrayList<Agent> agents;
    public final ArrayList<Box> boxes;
    public final Action[] jointAction;
    private final StateConfig stateConfig;
    private final int hash;
    
    @Getter
    private final int g;

    public State(@NonNull State parent, ArrayList<Agent> agents, ArrayList<Box> boxes, @NonNull Action[] jointActions){
        this.parent = parent;
        this.agents = agents;
        this.boxes = boxes;
        this.jointAction = jointActions;
        this.g = parent.g + 1;
        this.stateConfig = parent.stateConfig;
        this.hash = stateConfig.getHash().apply(this);
    }

    public State(ArrayList<Agent> agents, ArrayList<Box> boxes, @NonNull StateConfig stateConfig){
        this.parent = null;
        this.agents = agents;
        this.boxes = boxes;
        this.jointAction = null;
        this.g = 0;
        this.stateConfig = stateConfig;
        this.hash = stateConfig.getHash().apply(this);
    }

    public State(ArrayList<Agent> agents, ArrayList<Box> boxes){
        this(agents, boxes, new StateConfig());
    }

    public int g()
    {
        return this.g;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        var commaSeparate = Collectors.joining(", ");
        var newline = System.lineSeparator();

        sb.append("Agents: ");
        sb.append(agents.stream().map(Agent::toString).collect(commaSeparate));
        sb.append(newline);

        sb.append("Boxes: ");
        sb.append(boxes.stream().map(Box::toString).collect(commaSeparate));
        sb.append(newline);

        return sb.toString();
    }

    public boolean equivalent(State other){
        // same step
        if(this.g != other.g) return false;

        // All agents must be equal and in the same order, due to joint action order
        if(!other.agents.equals(this.agents)) return false;

        // Joint actions must be identical
        if(!Arrays.equals(other.jointAction, this.jointAction)) return false;

        // All boxes must be there, but order may vary
        if(other.boxes.size() != this.boxes.size()) return false;
        for(var box: other.boxes) if(!this.boxes.contains(box)) return false;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof State s && equivalent(s);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
