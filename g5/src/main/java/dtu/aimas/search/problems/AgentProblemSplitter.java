package dtu.aimas.search.problems;

import dtu.aimas.common.Agent;
import dtu.aimas.search.Problem;

import java.util.List;
import java.util.stream.Collectors;

public class AgentProblemSplitter implements ProblemSplitter {
    public List<Problem> split(Problem problem) {
        assert problem.agents.stream().map(a -> a.color).collect(Collectors.toSet()).size() == problem.agents.size()
                : "Agents must have unique colors";
        
        return problem.agents.stream()
                .map(a -> singleAgent(problem, a))
                .toList();
    }

    private Problem singleAgent(Problem source, Agent agent) {
        var agents = List.of(agent);
        var boxes = source.boxes.stream().filter(b ->
                b.color == agent.color).collect(Collectors.toList());
        var goals = new char[source.goals.length][source.goals[0].length];

        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var symbol = source.goals[row][col];
                if (symbol == 0) continue;
                if (symbol == agent.label){
                    goals[row][col] = symbol;
                }  else
                if (boxes.stream().anyMatch(b -> b.label == symbol)) {
                    goals[row][col] = symbol;
                }
            }
        }
        return new Problem(agents, boxes, goals, source);
    }
}
