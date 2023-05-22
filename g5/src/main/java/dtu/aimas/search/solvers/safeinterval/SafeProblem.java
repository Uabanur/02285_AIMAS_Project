package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;

import java.util.*;
import java.util.stream.Collectors;

public class SafeProblem extends Problem {
    private final Map<Position, List<TimeInterval>> conflictingIntervals;
    public SafeProblem(Collection<Agent> agentCollection, Collection<Box> boxCollection,
                       boolean[][] walls, char[][] goals,
                       Map<Position, List<TimeInterval>> conflictingIntervals) {
        super(agentCollection, boxCollection, walls, goals);
        this.conflictingIntervals = conflictingIntervals;
    }

    public SafeProblem(Collection<Agent> agentCollection, Collection<Box> boxCollection,
                       boolean[][] walls, char[][] goals) {
        this(agentCollection, boxCollection, walls, goals, new HashMap<>());
    }

    public static Optional<SafeProblem> from(Problem problem, ReservedCell reservedCell) {
        var child = from(problem);
        boolean change = child.updateInterval(reservedCell);
//        IO.debug("Interval count: %d", child.conflictingIntervals.values().stream().mapToInt(List::size).sum());
        return change ? Optional.of(child) : Optional.empty();
    }

    public static Optional<SafeProblem> from(Problem problem, List<ReservedCell> reservedCells) {
        var child = from(problem);
        var changes = false;
        for(var reservation: reservedCells){
            changes |= child.updateInterval(reservation);
        }
//        IO.debug("Interval count: %d", child.conflictingIntervals.values().stream().mapToInt(List::size).sum());
        return changes ? Optional.of(child) : Optional.empty();
    }

    private boolean updateInterval(ReservedCell reservation) {
        var intervals = conflictingIntervals.get(reservation.getCell());
        if(intervals == null){
            conflictingIntervals.put(reservation.getCell(),
                    new ArrayList<>(){{add(reservation.getInterval());}}
            );
            return true;
        }

        if (intervals.contains(reservation.getInterval())){
            return false;
        }

        intervals.add(reservation.getInterval());
        return true;
    }

    public static SafeProblem from(Problem problem) {
        if (problem instanceof SafeProblem safeProblem){
            return new SafeProblem(
                    safeProblem.agents,
                    safeProblem.boxes,
                    safeProblem.walls,
                    safeProblem.goals,
                    safeProblem.conflictingIntervals.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e->new ArrayList<>(e.getValue())))
            );
        }

        return new SafeProblem(
                problem.agents,
                problem.boxes,
                problem.walls,
                problem.goals);
    }

    public boolean isFree(Position pos, Agent agent, int timeStep) {
        if(!super.isFree(pos, agent, timeStep)) return false;

        var intervals = conflictingIntervals.get(pos);
        if(intervals == null) return true;
        return intervals.stream().noneMatch(i -> i.contains(timeStep));
    }

    public boolean validGoalState(State state){
        final int step = state.g();
        for(var agent: state.agents){
            var hasFutureConflict = conflictingIntervals
                    .getOrDefault(agent.pos, List.of())
                    .stream().anyMatch(i -> i.futureOverlap(step));
            if(hasFutureConflict) return false;
        }
        for(var box: state.boxes){
            var hasFutureConflict = conflictingIntervals
                    .getOrDefault(box.pos, List.of())
                    .stream().anyMatch(i -> i.futureOverlap(step));
            if(hasFutureConflict) return false;
        }

        return true;
    }

    @Override
    public Problem copyWith(List<Agent> agents, List<Box> boxes, char[][] goals) {
        return new SafeProblem(agents, boxes, this.walls, goals, conflictingIntervals);
    }

    @Override
    public Problem copyWith(List<Agent> agents, List<Box> boxes, char[][] goals, boolean[][] walls) {
        return new SafeProblem(agents, boxes, walls, goals, conflictingIntervals).precompute();
    }

    public SafeProblem copy(){
        var clone = new SafeProblem(agents, boxes, walls, goals, new HashMap<>(conflictingIntervals.size()));
        for(var entry: conflictingIntervals.entrySet()){
            // todo instead of interval list, it could walk up the ancestor intervals
            clone.conflictingIntervals.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return clone;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        var newline = System.lineSeparator();
        sb.append(super.toString()).append(newline);
        if(!conflictingIntervals.isEmpty()) sb.append("reserves:").append(newline);
        for(var entry: conflictingIntervals.entrySet()){
            var pos = entry.getKey();
            sb.append(" ").append(pos).append(":");
            for(var interval: entry.getValue()){
                sb.append(" ").append(interval);
            }
            sb.append(newline);
        }

        return sb.toString();
    }
}
