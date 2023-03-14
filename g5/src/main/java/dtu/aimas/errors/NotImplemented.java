package dtu.aimas.errors;

public class NotImplemented extends Throwable {
    public NotImplemented(String msg){ super(msg); }
    public NotImplemented(){ this("Feature is not yet implemented"); }
}
