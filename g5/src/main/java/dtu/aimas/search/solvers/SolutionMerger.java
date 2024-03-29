package dtu.aimas.search.solvers;

import dtu.aimas.common.Agent;
import dtu.aimas.search.Action;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.Attempt;
import dtu.aimas.search.solvers.graphsearch.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SolutionMerger {

    public static StateSolution mergeAttempts(List<Attempt> attempts){
        var solutions = attempts.stream()
                .map(a -> a.getSolution().get())
                .toList();
        return mergeSolutions(solutions);
    }

    public static StateSolution mergeSolutions(List<StateSolution> solutions){
        var solutionLength = solutions.stream()
                .mapToInt(StateSolution::size)
                .max()
                .orElse(0);

        var states = new State[solutionLength];
        for(var step = 0; step < solutionLength; step++){
            var parent = step == 0 ? null : states[step-1];
            states[step] = combinedState(solutions, step, parent);
        }

        return new StateSolution(states);
    }

    public static StateSolution pruneInactiveStates(StateSolution stateSolution) {
        var states = new ArrayList<State>();
        states.add(stateSolution.getState(0));

        for(var step = 1; step < stateSolution.size(); step++){
            var state = stateSolution.getState(step);
            assert state.jointAction != null;
            if(Arrays.stream(state.jointAction).anyMatch(a -> a != Action.NoOp)) {
                var parent = states.get(states.size()-1);
                states.add(new State(parent, state.agents, state.boxes, state.jointAction));
            }
        }

        return new StateSolution(states.toArray(State[]::new));
    }

    private static State combinedState(List<StateSolution> solutions, int step, State parent){
        var agents = solutions.stream()
                .flatMap(s -> s.getState(Math.min(s.size()-1, step)).agents.stream())
                .collect(Collectors.toCollection(ArrayList::new));

        var boxes = solutions.stream()
                .flatMap(s -> s.getState(Math.min(s.size()-1,step)).boxes.stream())
                .collect(Collectors.toCollection(ArrayList::new));

        // Sort agents and joint actions the same
        var jointAction = new Action[agents.size()];
        var agentArray = new Agent[agents.size()];
        var filled = 0;
        for(var solution: solutions){
            var solutionFinished = step >= solution.size();
            var state = solution.getState(Math.min(solution.size()-1, step));
            for(var i = 0; i < state.agents.size(); i++){
                agentArray[filled + i] = state.agents.get(i);
                jointAction[filled + i] = step == 0 || solutionFinished ? Action.NoOp : state.jointAction[i];
            }
            filled += state.agents.size();
        }

        var indices = IntStream.range(0, agents.size()).boxed().toArray(Integer[]::new);
        Arrays.sort(indices, new AgentIndexComparator(agentArray));
        var sortedJointAction = new Action[indices.length];
        var sortedAgents = new Agent[indices.length];
        for(var i = 0; i < indices.length; i++){
            sortedJointAction[i] = jointAction[indices[i]];
            sortedAgents[i] = agentArray[indices[i]];
        }

        var sortedAgentsList = Stream.of(sortedAgents).collect(Collectors.toCollection(ArrayList::new));
        if (step == 0) return new State(sortedAgentsList, boxes);
        return new State(parent, sortedAgentsList, boxes, sortedJointAction);
    }

    static class AgentIndexComparator implements Comparator<Integer>
    {
        private final Agent[] agents;
        AgentIndexComparator(Agent[] agents) { this.agents = agents; }

        @Override
        public int compare(Integer first, Integer second) {
            return Character.compare(this.agents[first].label, this.agents[second].label);
        }
    }

    public static StateSolution sequentialJoin(List<StateSolution> solutions) {
        var solutionLength = solutions.stream()
                .mapToInt(StateSolution::size).map(s -> s-1)
                .sum();

        var states = new State[solutionLength+1];
        states[0] = solutions.get(0).getState(0);
        int step = 1;
        for(StateSolution sol : solutions) {
            for(int i = 1; i < sol.size(); i++) {
                State s = sol.getState(i);
                states[step] = new State(states[step-1], s.agents, s.boxes, s.jointAction);
                step+=1;
            }
        }
        return new StateSolution(states);
    }
}
