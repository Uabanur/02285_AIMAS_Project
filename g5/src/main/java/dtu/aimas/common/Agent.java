package dtu.aimas.common;

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

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Agent)) return false;

        var other = (Agent)o;
        return this.pos == other.pos && this.color == other.color;
    }

    public String toSimpleString(){
        return String.format("(%d,%d|%s)", pos.row, pos.col, color.name());
    }
}
