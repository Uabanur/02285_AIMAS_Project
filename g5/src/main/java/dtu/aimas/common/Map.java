package dtu.aimas.common;

public class Map {
    public final boolean[][] walls;

    public Map(boolean[][] walls) {
        this.walls = walls;
    }

    public static boolean isWall(char symbol) {
        return symbol == '+';
    }
}
