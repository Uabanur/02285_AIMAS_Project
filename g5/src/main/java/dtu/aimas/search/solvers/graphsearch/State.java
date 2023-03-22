package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import dtu.aimas.common.*;
import dtu.aimas.search.Action;
import lombok.Getter;

public class State {
    public final State parent;
    public ArrayList<Agent> agents;
    public ArrayList<Box> boxes;
    public Action[] jointAction;
    
    @Getter
    private final int g;

    public State(State parent, ArrayList<Agent> agents, ArrayList<Box> boxes, Action[] jointActions){
        this.parent = parent;
        this.agents = agents;
        this.boxes = boxes;
        this.jointAction = jointActions;
        this.g = parent.g + 1;
    }

    public State(ArrayList<Agent> agents, ArrayList<Box> boxes){
        this.parent = null;
        this.agents = agents;
        this.boxes = boxes;
        this.jointAction = null;
        this.g = 0;
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
        sb.append(agents.stream().map(x -> x.toSimpleString()).collect(commaSeparate));
        sb.append(newline);

        sb.append("Boxes: ");
        sb.append(boxes.stream().map(x -> x.toSimpleString()).collect(commaSeparate));
        sb.append(newline);

        return sb.toString();
    }

    public String printActions(){
        StringBuilder sb = new StringBuilder("Actions: ");
        for (Action action : jointAction)
            sb.append(action.name + " ");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return Objects.equals(agents, state.agents) &&
               Objects.equals(boxes, state.boxes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agents, boxes);
    }
    
}
