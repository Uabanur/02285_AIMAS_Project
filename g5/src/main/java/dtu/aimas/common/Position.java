package dtu.aimas.common;

import java.util.Objects;

public class Position {
    public int row;
    public int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public String toSimpleString() {
        return String.format("%d,%d", row, col);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if(!(o instanceof Position)) return false;
        Position c = (Position) o;
        return c.row == this.row && c.col == this.col;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", row, col);
    }

}
