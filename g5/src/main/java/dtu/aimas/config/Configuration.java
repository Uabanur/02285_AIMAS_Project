package dtu.aimas.config;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.BFS;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter
public class Configuration {
    public Configuration(){
        solver = new BFS();
        groupName = "Group5";
        logLevel = LogLevel.Information;
        debugServerMessages = false;
    }

    @NonNull
    private String groupName;

    @NonNull
    private Solver solver;

    private LogLevel logLevel;
    private Boolean debugServerMessages;

    public void configureIO(){
        IO.logLevel = logLevel;
        IO.debugServerMessages = debugServerMessages;
    }

    @Override
    public String toString() {
        return String.format("Configuration{Solver=%s; GroupName=%s; LogLevel=%s; DebugServerMessages=%b}", 
            solver.getClass().getSimpleName(),
            groupName,
            logLevel.name(),
            debugServerMessages
        );
    }
}
