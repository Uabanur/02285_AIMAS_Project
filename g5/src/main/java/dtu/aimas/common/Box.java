package dtu.aimas.common;

import java.util.Objects;
import java.util.UUID;

public class Box extends DomainObject {
    private Box(Position pos, Color color, char type, UUID uid){
        super(pos, color, type, uid);
    }

    public Box(Position pos, Color color, char type) {
        this(pos, color, type, UUID.randomUUID());
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
        return Objects.hash(pos, color, label, uid);
    }

    public Box clone() {
        return new Box(pos.clone(), color, label, uid);
    }
    public Box clone(int row, int col){
        return clone(new Position(row, col));
    }
    public Box clone(Position newPosition){
        return new Box(newPosition, color, label, uid);
    }
    public Box clone(Color newColor){
        return new Box(pos.clone(), newColor, label, uid);
    }
}
