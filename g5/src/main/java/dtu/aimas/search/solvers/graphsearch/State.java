package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import dtu.aimas.common.*;
import dtu.aimas.search.Action;
import lombok.Getter;

public class State {
    public final State parent;
    public final ArrayList<Agent> agents;
    public final ArrayList<Box> boxes;
    public final Action[] jointAction;
    private final int hash;
    
    @Getter
    private final int g;

    public State(State parent, ArrayList<Agent> agents, ArrayList<Box> boxes, Action[] jointActions){
        this.parent = parent;
        this.agents = agents;
        this.boxes = boxes;
        this.jointAction = jointActions;
        this.g = parent == null ? 0 : parent.g + 1;
        this.hash = Objects.hash(agents, boxes, g, Arrays.hashCode(jointActions), parent == null ? null: parent.hash);
    }

    public State(ArrayList<Agent> agents, ArrayList<Box> boxes){
        this(null, agents, boxes, null);
//        this.parent = null;
//        this.agents = agents;
//        this.boxes = boxes;
//        this.jointAction = null;
//        this.g = 0;
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
        sb.append(agents.stream().map(x -> x.toString()).collect(commaSeparate));
        sb.append(newline);

        sb.append("Boxes: ");
        sb.append(boxes.stream().map(x -> x.toString()).collect(commaSeparate));
        sb.append(newline);

        return sb.toString();
    }

    public String printActions(){
        StringBuilder sb = new StringBuilder("Actions: ");
        for (Action action : jointAction)
            sb.append(action.name + " ");
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
        for(var box: other.boxes) if(!this.boxes.contains(box)) return false;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        return hashCode() == o.hashCode();
//        return Objects.equals(agents, state.agents) &&
//               Objects.equals(boxes, state.boxes) &&
//               g == state.g();
    }

    @Override
    public int hashCode() {
//        return Objects.hash(agents, boxes, g);
        return hash;
    }
    
}
