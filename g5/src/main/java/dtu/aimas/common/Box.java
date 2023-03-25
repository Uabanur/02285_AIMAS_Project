package dtu.aimas.common;

import java.util.Objects;

public class Box {
    public final Position pos;
    public final Color color;
    public final char label;

    public Box(Position pos, Color color, char type) {
        this.pos = pos;
        this.color = color;
        this.label = type;
    }

    public static boolean isLabel(char symbol) {
        return 'A' <= symbol && symbol <= 'Z';
    }

    public String toSimpleString(){
        return String.format("(%s:%c|%s)", pos.toSimpleString(), label, color.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Box)) return false;
        Box box = (Box) o;
        return label == box.label &&
                Objects.equals(pos, box.pos) &&
                color == box.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, color, label);
    }
}
