package dtu.aimas.errors;

public class SolutionNotFound extends Throwable {
    public SolutionNotFound(String message){ super(message); }
    public SolutionNotFound(){ this("Solution not foudn"); }
}
