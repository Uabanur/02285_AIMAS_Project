package dtu.aimas.common;

import java.util.Objects;

public class Box extends DomainObject {

    public Box(Position pos, Color color, char type) {
        super(pos, color, type);
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
        return new Box(new Position(pos.row, pos.col), color, label);
    }
}
