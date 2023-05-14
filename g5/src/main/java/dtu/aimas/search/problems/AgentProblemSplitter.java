package dtu.aimas.search.problems;

import dtu.aimas.search.Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgentProblemSplitter implements ProblemSplitter{
    @Override
    public List<Problem> split(Problem problem) {
        var problems = new ArrayList<Problem>();
        var agentColors = problem.agents.stream().map(a -> a.color).collect(Collectors.toSet());
        var strayBoxes = problem.boxes.stream().filter(b -> agentColors.stream().noneMatch(c -> b.color == c)).toList();

        for(var agent: problem.agents){
            var agentBoxes = problem.boxes.stream().filter(b -> b.color == agent.color);
            var boxes = Stream.concat(strayBoxes.stream(), agentBoxes).toList();

            var boxGoals = problem.boxGoals.stream().filter(g -> boxes.stream().anyMatch(b -> b.label == g.label)).toList();
            var agentGoals = problem.agentGoals.stream().filter(a -> a.label == agent.label).toList();

            var goalTable = new char[problem.goals.length][problem.goals[0].length];
            for(var goal: boxGoals) goalTable[goal.destination.row][goal.destination.col] = goal.label;
            for(var goal: agentGoals) goalTable[goal.destination.row][goal.destination.col] = goal.label;

            problems.add(problem.copyWith(List.of(agent), boxes, goalTable));
        }

        return problems;
    }
}
