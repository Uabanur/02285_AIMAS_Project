package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Result;
import dtu.aimas.search.solutions.StateSolution;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SafePair
{
    public SafePair(SafeProblem safeProblem, Result<StateSolution> solution){
        this.safeProblem = safeProblem;
        this.solution = solution;
    }

    private SafeProblem safeProblem;
    private Result<StateSolution> solution;

    public SafePair copy(){
        // solutions are not modified.
        return new SafePair(safeProblem.copy(), solution);
    }
}
