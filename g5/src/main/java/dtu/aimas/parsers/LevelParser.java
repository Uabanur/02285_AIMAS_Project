package dtu.aimas.parsers;

import java.io.Reader;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;

public abstract class LevelParser {
    protected abstract Result<Problem> parseInput(Reader level);
    public Result<Problem> parse(Reader level){
        return parseInput(level)
                .map(this::validateLevel)
                .map(Problem::precompute);
    }

    private Problem validateLevel(Problem problem){
        assert problem.boxes.stream().allMatch(b -> b.color != null) : "Boxes cannot have null color";
        assert problem.agentGoals.size() <= problem.agents.size() : "Not enough agents to fill all goals";
        assert problem.boxGoals.size() <= problem.boxes.size() : "Not enough boxes to fill all goals";

        for(var agentGoal: problem.agentGoals){
            assert problem.agents.stream().anyMatch(a -> a.label == agentGoal.label);
        }

        for(var boxGoal: problem.boxGoals){
            assert problem.boxes.stream().anyMatch(b -> b.label == boxGoal.label);
        }

        return problem;
    }
}