package dtu.aimas.common;

import java.util.Objects;

public class Box extends DomainObject {
    public final int id;

    public Box(Position pos, Color color, char type, int id) {
        super(pos, color, type);
        this.id = id;
    }

    public static boolean isLabel(char symbol) {
        return 'A' <= symbol && symbol <= 'Z';
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

    public Box clone() {
        return new Box(new Position(pos.row, pos.col), color, label, id);
    }
}
