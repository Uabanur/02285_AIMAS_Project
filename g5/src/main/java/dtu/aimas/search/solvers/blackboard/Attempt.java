package dtu.aimas.search.solvers.blackboard;

import dtu.aimas.common.Result;
import dtu.aimas.search.solutions.StateSolution;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class Attempt {
    private final Result<StateSolution> solution;
    @Setter
    private List<StateSolution> conflicts;

    public Attempt(Result<StateSolution> solution) {
        this.solution = solution;
    }
}
