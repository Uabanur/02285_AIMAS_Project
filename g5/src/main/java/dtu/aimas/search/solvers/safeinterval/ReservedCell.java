package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Position;
import lombok.Getter;

@Getter
public class ReservedCell{
    private final Position cell;
    private final TimeInterval interval;
    public ReservedCell(Position cell, TimeInterval interval){
        this.cell = cell;
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "pos: "+cell + ". steps: " + interval;
    }
}
