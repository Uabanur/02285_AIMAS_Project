package dtu.aimas.common;

import java.util.UUID;

// This class is the superclass of Agent and Box.
// It exists to allow the use of polymorphism in various parts of the code, when iterating over agents and boxes.
public class DomainObject {

    public UUID uid;
    public Position pos;
    public Color color;
    public char label;
    
    public DomainObject(Position pos, Color color, char label, UUID uid) {
        this.pos = pos;
        this.color = color;
        this.label = label;
    }

    public String toString(){
        return String.format("(%s:%c|%s)", pos.toSimpleString(), label, color.name());
    }
}
