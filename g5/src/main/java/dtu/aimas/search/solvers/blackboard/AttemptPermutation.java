package dtu.aimas.search.solvers.blackboard;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class AttemptPermutation {
    private int[] indices;

    public AttemptPermutation(int[] indices) {
        this.indices = indices;
    }

    public List<Attempt> getAttempts(Plan[] plans){
        assert indices.length == plans.length;

        return IntStream.range(0, indices.length)
                .mapToObj(i -> plans[i].getAttempts().get(indices[i]))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttemptPermutation other)) return false;
        return Arrays.equals(indices, other.indices);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(indices);
    }
}
