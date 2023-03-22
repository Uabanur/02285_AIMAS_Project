package dtu.aimas.common;

import java.util.Objects;

public class Agent {
    public Position pos;
    public Color color;

    public Agent(Position pos, Color color) {
        this.pos = pos;
        this.color = color;
    }

    public static boolean isLabel(char symbol) {
        return '0' <= symbol && symbol <= '9';
    }

    public String toSimpleString(){
        return String.format("(%s|%s)", pos.toSimpleString(), color.name());
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
}
