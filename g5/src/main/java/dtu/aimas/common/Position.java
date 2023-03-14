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
}
