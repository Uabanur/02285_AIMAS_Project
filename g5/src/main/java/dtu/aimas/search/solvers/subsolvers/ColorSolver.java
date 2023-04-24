package dtu.aimas.search.solvers.subsolvers;

import dtu.aimas.common.*;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.SolverMinLength;
import dtu.aimas.search.solvers.heuristics.Cost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ColorSolver implements SolverMinLength {
    private final Function<Cost, SolverMinLength> subSolverGenerator;
    private final Cost baseCost;

    public ColorSolver(Function<Cost, SolverMinLength> subSolverGenerator, Cost baseCost){
        this.subSolverGenerator = subSolverGenerator;
        this.baseCost = baseCost;
    }

    @Override
    public Result<Solution> solve(Problem initial) {
        return solve(initial, 0);
    }

    @Override
    public Result<Solution> solve(Problem initial, int minSolutionLength) {
        var originalColor = initial.agents.stream().findFirst().map(a -> a.color).orElse(Color.values()[0]);
        assert initial.agents.stream().allMatch(a -> a.color == originalColor);

        var assigned = assignBoxes(initial);
        var agents = new ArrayList<Agent>(initial.agents.size());
        var boxes = new ArrayList<Box>(initial.boxes.size());

        var colorIndex = 0;
        for(var agent: initial.agents){
            var newColor = Color.values()[colorIndex++];

            agents.add(agent.clone(newColor));

            for(var box: assigned.getOrDefault(agent, List.of())){
                boxes.add(box.clone(newColor));
            }
        }

        var newProblem = new Problem(agents, boxes, initial.goals, initial);
        var solution = subSolverGenerator.apply(baseCost).solve(newProblem, minSolutionLength);
        return solution.map(s -> reassignColor((StateSolution)s, originalColor));
    }

    private StateSolution reassignColor(StateSolution solution, Color originalColor) {
        for(var i = 0; i < solution.size(); i++){
            var state = solution.getState(i);
            state.boxes.forEach(b -> b.color = originalColor);
            state.agents.forEach(a -> a.color = originalColor);
        }
        return solution;
    }

    private HashMap<Agent, List<Box>> assignBoxes(Problem problem){
        // TODO improve this simple assign to closest agent
        var result = new HashMap<Agent, List<Box>>();
        for(var box: problem.boxes){
            Agent agent = closestAgent(box, problem);
            if(result.containsKey(agent)){
                result.get(agent).add(box);
            } else {
                result.put(agent, new ArrayList<>() {{
                    add(box);
                }});
            }
        }

        return result;
    }

    private Agent closestAgent(Box box, Problem problem) {
        Agent chosenAgent = null;
        var minDistance = Integer.MAX_VALUE;

        for(var agent: problem.agents){
            var distance = problem.admissibleDist(agent.pos, box.pos);
            if (distance >= minDistance) continue;

            minDistance = distance;
            chosenAgent = agent;
        }

        assert chosenAgent != null : "Box has no closest agent";
        return chosenAgent;
    }
}
