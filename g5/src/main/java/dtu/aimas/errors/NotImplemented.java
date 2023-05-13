package dtu.aimas.errors;

public class NotImplemented extends RuntimeException {
    public NotImplemented(String msg){ super(msg); }
    public NotImplemented(){ this("Feature is not yet implemented"); }
}
