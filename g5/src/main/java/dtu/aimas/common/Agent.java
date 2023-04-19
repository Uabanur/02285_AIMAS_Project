package dtu.aimas.common;

import java.util.Objects;

public class Agent extends DomainObject {

    public Agent(Position pos, Color color, char label) {
        super(pos, color, label);
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
        return this.pos.equals(other.pos) && this.color == other.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, color);
    }

    @Override
    public Agent clone() {
        return new Agent(new Position(pos.row, pos.col), color, label);
    }
}
