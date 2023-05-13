package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;

import java.util.List;

public record ConflictInterval(
        List<Problem> involvedProblems,
        Position cell,
        TimeInterval interval
){
    @Override
    public String toString() {
        return "pos: "+cell + ". steps: " + interval;
    }
}
