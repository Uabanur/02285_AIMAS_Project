package dtu.aimas.search.solvers.blackboard;

import dtu.aimas.search.solvers.ConflictChecker;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class AttemptPermutation implements Comparable<AttemptPermutation> {
    private final int[] indices;
    private final int solutionHash;
    private final int conflicts;

    public AttemptPermutation(int[] indices, Plan[] plans, StateSpace space) {
        this.indices = indices;
        this.conflicts = evaluateConflicts(plans, space);

        var solutions = getAttempts(plans).stream().map(a -> a.getSolution().getOrElse(() -> null)).toArray();
        solutionHash = Objects.hash(solutions);
    }

    private int evaluateConflicts(Plan[] plans, StateSpace space){
        var attempts = this.getAttempts(plans);
        var count = 0;
        for(var attempt: attempts){
            var conflicts = ConflictChecker.getConflicts(attempt, attempts, space);
            count += conflicts.size();
            attempt.setConflicts(conflicts);
        }
        return count;
    }

    public List<Attempt> getAttempts(Plan[] plans){
        assert indices.length == plans.length;

        return IntStream.range(0, indices.length)
                .mapToObj(i -> plans[i].getAttempt(indices[i]))
                .toList();
    }

    public AttemptPermutation transfer(int position, int value, Plan[] plans, StateSpace space){
        var indices = this.indices.clone();
        indices[position] = value;
        return new AttemptPermutation(indices, plans, space);
    }

    @Override
    public String toString() {
        return Arrays.toString(indices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttemptPermutation other)) return false;
        return hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return solutionHash;
    }

    @Override
    public int compareTo(AttemptPermutation o) {
        return Integer.compare(this.conflicts, o.conflicts);
    }
}
