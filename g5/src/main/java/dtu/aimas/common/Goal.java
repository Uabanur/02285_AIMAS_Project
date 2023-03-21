package dtu.aimas.common;

public class Goal {
    public final char label;
    public final Position destination;

    public Goal(char label, Position destination){
        this.label = label;
        this.destination = destination;
    }

    public boolean isSatisfied(Box box){
        return box.type == label && box.pos == destination;
    }

    public boolean isSatisfied(Agent agent){
        return agent.pos == destination;
    }

    public String toSimpleString(){
        return String.format("(%d,%d|%s)", destination.row, destination.col, label);
    }

}
