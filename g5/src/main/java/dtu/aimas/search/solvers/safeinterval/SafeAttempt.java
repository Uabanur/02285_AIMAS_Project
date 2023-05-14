package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Result;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.Attempt;
import lombok.Getter;

@Getter
public class SafeAttempt extends Attempt  {
    private final SafeProblem safeProblem;

    public SafeAttempt(SafeProblem safeProblem, Result<StateSolution> solution) {
        super(solution);
        this.safeProblem = safeProblem;
    }
}
