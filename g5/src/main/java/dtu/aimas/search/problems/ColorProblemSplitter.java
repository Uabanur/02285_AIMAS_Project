package dtu.aimas.search.problems;

import dtu.aimas.common.Color;
import dtu.aimas.search.Problem;

import java.util.List;
import java.util.stream.Collectors;

public class ColorProblemSplitter implements ProblemSplitter {
    @Override
    public List<Problem> split(Problem problem) {
        return problem.agents.stream()
                .map(a -> a.color)
                .collect(Collectors.toSet()) // remove duplicates
                .stream()
                .map(color -> singleColor(problem, color))
                .toList();
    }

    private Problem singleColor(Problem source, Color color) {
        var agents = source.agents.stream().filter(a -> a.color == color).collect(Collectors.toList());
        var boxes = source.boxes.stream().filter(b -> b.color == color).collect(Collectors.toList());
        var goals = new char[source.goals.length][source.goals[0].length];

        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var symbol = source.goals[row][col];
                if (symbol == 0) continue;
                if (agents.stream().anyMatch(a -> a.label == symbol)) {
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
