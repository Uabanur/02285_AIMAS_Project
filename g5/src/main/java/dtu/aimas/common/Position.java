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

    public void updateBy(int rowDelta, int colDelta){
        this.row += row;
        this.col += col;
    }

    // just a placeholder, to delete/change
    public Position movePosition(int rowDelta, int colDelta){
        return new Position(this.row + rowDelta, this.col + colDelta);
    }

    public Position pullPosition(int rowDelta, int colDelta){
        return new Position(this.row - rowDelta, this.col - colDelta);
    }

    // just for testing
    public Position clone(){
        return new Position(row, col);
    }

}
