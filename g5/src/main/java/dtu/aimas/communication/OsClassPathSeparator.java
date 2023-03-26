package dtu.aimas.communication;

import dtu.aimas.errors.UnreachableState;

public class OsClassPathSeparator {
    public static String get(){
        var os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")){
            return ";";
        }
        if(os.contains("mac")){
            return ";";
        }
        if(os.contains("nix") || os.contains("nux") || os.contains("aix")){
            return ":";
        }

        throw new UnreachableState();
    }
}
