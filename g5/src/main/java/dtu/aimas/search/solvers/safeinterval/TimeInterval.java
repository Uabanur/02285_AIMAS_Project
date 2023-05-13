package dtu.aimas.search.solvers.safeinterval;

public record TimeInterval(int start, int end) {
    /**
     * @param step timestep
     * @return if in range, start included, end excluded.
     */
    public boolean contains(int step){
        return start <= step && step < end;
    }
    public boolean futureOverlap(int step){
        return step < end;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", start, end);
    }
}
