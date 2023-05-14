package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.StateSolution;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SafePlan {
    private final Problem problem;
    private final List<SafeAttempt> attempts;

    public SafePlan(Problem problem, SafeAttempt attempt) {
//        assert !(problem instanceof SafeProblem) : "Root problem should be the original";
        this.problem = problem;
        attempts = new ArrayList<>(){{add(attempt);}};
    }

    public SafePlan(Problem problem, Result<StateSolution> solution) {
        this(problem, new SafeAttempt(SafeProblem.from(problem), solution));
    }

    public SafeAttempt getAttempt(int i){
        return attempts.get(i);
    }
    public void addAttempt(SafeAttempt attempt){
        attempts.add(attempt);
    }

    public int lastAttemptIndex(){
        return attempts.size()-1;
    }
}
