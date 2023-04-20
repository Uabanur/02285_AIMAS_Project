package dtu.aimas.search.solvers.blackboard;

import java.util.ArrayList;
import java.util.List;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.StateSolution;
import lombok.Getter;

@Getter
public class Plan {
    private final Problem problem;
    private final List<Attempt> attempts;

    public Plan(Problem problem, Attempt attempt) {
        this.problem = problem;
        attempts = new ArrayList<>(){{add(attempt);}};
    }

    public Plan(Problem problem, Result<StateSolution> solution) {
        this(problem, new Attempt(solution));
    }

    public Attempt getAttempt(int i){
        return attempts.get(i);
    }
    public void addAttempt(Attempt attempt){
        attempts.add(attempt);
    }

    public int lastAttemptIndex(){
        return attempts.size()-1;
    }
}
