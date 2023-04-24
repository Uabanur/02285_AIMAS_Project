package dtu.aimas.common;

import java.util.Objects;
import java.util.UUID;

public class Agent extends DomainObject {
    private Agent(Position pos, Color color, char label, UUID uid){
        super(pos, color, label, uid);
    }
    public  Agent(Position pos, Color color, char label) {
        this(pos, color, label, UUID.randomUUID());
    }

    public static boolean isLabel(char symbol) {
        return '0' <= symbol && symbol <= '9';
    }

    public String toString(){
        return String.format("(%s:%c|%s)", pos.toSimpleString(), label, color.name());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Agent)) return false;

        var other = (Agent)o;
        return this.pos.equals(other.pos)
                && this.color == other.color
                && this.label == other.label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, color, label, uid);
    }

    public Agent clone() {
        return new Agent(pos.clone(), color, label, uid);
    }

    public Agent clone(int row, int col){
        return clone(new Position(row, col));
    }
    public Agent clone(Position newPosition){
        return new Agent(newPosition, color, label, uid);
    }
    public Agent clone(Color newColor){
        return new Agent(pos.clone(), newColor, label, uid);
    }
}
