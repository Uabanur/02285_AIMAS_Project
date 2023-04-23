package dtu.aimas.search.solvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Position;

public class Constraint {
    private final Map<String, List<Position>> constraints;

    private Constraint(Map<String, List<Position>> constraints){
        this.constraints = constraints;
    }
    public static Constraint empty(){
        return new Constraint(new HashMap<>());
    }

    private String createKey(Agent agent, int timeStep){
        return String.format("%c|%d", agent.label, timeStep);
    }

    public Constraint extend(Agent agent, Position position, int timeStep) {
        var key = createKey(agent, timeStep);
        var extendedConstraints = constraints.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> List.copyOf(e.getValue())));

        var positions = extendedConstraints.computeIfAbsent(key, s -> new ArrayList<>());
        positions.add(position);

        return new Constraint(extendedConstraints);
    }

    public boolean isReserved(Agent agent, Position position, int timeStep) {
        var reservedPositions = constraints.get(createKey(agent, timeStep));
        if (reservedPositions == null) return false;
        return reservedPositions.stream().anyMatch(p -> p.equals(position));
    }
    public List<Position> getReserved(Agent agent, int timeStep){
        return constraints.getOrDefault(createKey(agent, timeStep), new ArrayList<>());
    }

    @Override
    public String toString() {
        return "Constraint [constraints=" + constraints + "]";
    }
}
