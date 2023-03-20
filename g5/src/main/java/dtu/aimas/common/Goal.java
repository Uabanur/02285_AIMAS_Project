package dtu.aimas.common;

public class Goal {
    public final char label;
    public final Position destination;

    public Goal(char label, Position destination){
        this.label = label;
        this.destination = destination;
    }

    public String toSimpleString(){
        return String.format("(%d,%d|%s)", destination.row, destination.col, label);
    }

}
