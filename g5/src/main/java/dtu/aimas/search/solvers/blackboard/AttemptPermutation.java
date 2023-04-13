package dtu.aimas.search.solvers.blackboard;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class AttemptPermutation {
    private final int[] indices;
    private final int solutionHash;

    public AttemptPermutation(int[] indices, Plan[] plans) {
        this.indices = indices;
        var solutions = IntStream.range(0, indices.length)
                .mapToObj(i -> plans[i].getAttempt(indices[i]).getSolution().getOrElse(() -> null))
                .toArray();
        solutionHash = Objects.hash(solutions);
    }

    public List<Attempt> getAttempts(Plan[] plans){
        assert indices.length == plans.length;

        return IntStream.range(0, indices.length)
                .mapToObj(i -> plans[i].getAttempt(indices[i]))
                .toList();
    }

    public AttemptPermutation transfer(int position, int value, Plan[] plans){
        var indices = this.indices.clone();
        indices[position] = value;
        return new AttemptPermutation(indices, plans);
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
}
