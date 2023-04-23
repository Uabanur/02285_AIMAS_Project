package dtu.aimas.search.solvers.subsolvers;

import dtu.aimas.common.Box;
import dtu.aimas.common.Goal;

public record RankedBox(Box box, Goal goal, int rank) {
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        return (obj instanceof RankedBox r) && box.equals(r.box);
    }

    @Override
    public int hashCode() {
        return box.hashCode();
    }

}
