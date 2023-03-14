package dtu.aimas.common;

public class Box {
    public final Position pos;
    public final Color color;
    public final char type;

    public Box(Position pos, Color color, char type) {
        this.pos = pos;
        this.color = color;
        this.type = type;
    }

    public static boolean isLabel(char symbol) {
        return 'A' <= symbol && symbol <= 'Z';
    }
}
