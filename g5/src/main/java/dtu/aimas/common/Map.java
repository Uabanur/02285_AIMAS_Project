package dtu.aimas.common;

import dtu.aimas.search.Problem;

public class Map {
    public char[][] cells;

    public Map(char[][] cells) {
        this.cells = cells;
    }

    public static boolean isWall(char symbol) {
        return symbol == '+';
    }

    public static Map from(Problem problem){
        if(problem.walls.length == 0) return new Map(new char[0][0]);

        var walls = new char[problem.walls.length][problem.walls[0].length];

        for(var row = 0; row < walls.length; row++){
            for(var col = 0; col < walls[0].length; col++){
                if(problem.walls[row][col]) walls[row][col] = '+';
            }
        }

        return new Map(walls);
    }

    public boolean isFree(Position pos) {
        return isFree(pos.row, pos.col);
    }

    public boolean isFree(int row, int col) {
        return cells[row][col] == 0;
    }

    public void set(Position pos, char symbol) {
        set(pos.row, pos.col, symbol);
    }

    public void set(int row, int col, char symbol) {
        assert cells[row][col] != '+': "Walls cannot be overwritted";
        cells[row][col] = symbol;
    }

    public void clear(Position pos){
        clear(pos.row, pos.col);
    }

    public void clear(int row, int col){
        assert cells[row][col] != '+': "Walls cannot be removed";
        cells[row][col] = 0;
    }

    public void reset() {
        for(var row = 0; row < cells.length; row++){
            for(var col = 0; col < cells[0].length; col++){
                if(cells[row][col] == '+') continue;
                cells[row][col] = 0;
            }
        }
    }
}
