package dtu.aimas.search.solvers.blackboard;

import java.util.List;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Plan {
    private Agent agent;
    private Problem problem;
    private Result<Solution> solution;
    @Setter private List<Solution> conflicts;

    public Plan(Agent agent, Problem problem, Result<Solution> solution) {
        this.agent = agent;
        this.problem = problem;
        this.solution = solution;
        conflicts = List.of();
    }
}
