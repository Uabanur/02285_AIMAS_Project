package dtu.aimas.common;

public class Position {
    public int row;
    public int col;
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void update(int row, int col){
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if(!(o instanceof Position)) return false;
        Position c = (Position) o;
        return c.row == this.row && c.col == this.col;
    }

    @Override
    public int hashCode() {
        //Necessary to use hashed data structures
        //inspired by the javafx.util.Pair hashCode function
        int hash = 7;
        hash = 31 * hash + Integer.valueOf(row).hashCode();
        hash = 31 * hash + Integer.valueOf(col).hashCode();
        return hash;
    }
}
