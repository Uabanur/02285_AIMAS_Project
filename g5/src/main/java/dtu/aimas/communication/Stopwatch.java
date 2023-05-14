package dtu.aimas.communication;

public class Stopwatch {
    public static long getTimeMs(){
        return System.currentTimeMillis();
    }
    public static long getTimeSinceMs(long since){
        return getTimeMs() - since;
    }
}
