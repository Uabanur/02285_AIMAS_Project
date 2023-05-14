package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;
import lombok.Getter;

import java.util.List;

@Getter
public class ConflictInterval extends ReservedCell {
    private final List<Problem> involvedProblems;
    public ConflictInterval(List<Problem> involvedProblems, Position cell, TimeInterval interval){
        super(cell, interval);
        this.involvedProblems = involvedProblems;
    }
}
