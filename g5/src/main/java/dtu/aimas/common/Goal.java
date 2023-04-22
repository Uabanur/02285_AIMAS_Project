package dtu.aimas.common;

import java.util.Objects;

public class Goal {
    public final char label;
    public final Position destination;

    public Goal(char label, Position destination){
        this.label = label;
        this.destination = destination;
    }

    public String toSimpleString(){
        return String.format("(%d,%d:%s)", destination.row, destination.col, label);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Goal other = (Goal) obj;
        return label == other.label && Objects.equals(destination, other.destination);
    }
}
