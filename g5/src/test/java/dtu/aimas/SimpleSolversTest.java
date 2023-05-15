package dtu.aimas;

import org.junit.Ignore;
import org.junit.Test;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.communication.Stopwatch;
import dtu.aimas.helpers.LevelSolver;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.graphsearch.Focal;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;

public class SimpleSolversTest {
    private final Focal focalSolver = new Focal(new DistanceSumCost());
    private final AStar astarSolver = new AStar(new DistanceSumCost());

    private void benchmarkMaps(String[] maps){
        IO.logLevel = LogLevel.Debug;
        
        var focalTimes = new long[maps.length];
        var astarTimes = new long[maps.length];

        for(int i=0; i<maps.length; i++){
            var start = Stopwatch.getTimeMs();
            LevelSolver.testMap(maps[i], focalSolver);
            var timeElapsed = Stopwatch.getTimeSinceMs(start);
            focalTimes[i] = timeElapsed;



            start = Stopwatch.getTimeMs();
            LevelSolver.testMap(maps[i], astarSolver);
            timeElapsed = Stopwatch.getTimeSinceMs(start);
            astarTimes[i] = timeElapsed;
        }
        
        for(int i=0; i<maps.length; i++){
            IO.debug("Map: " + maps[i] + " Focal: " + focalTimes[i] + "[ms] AStar: " + astarTimes[i] + "[ms]");
        }
    }

    @Test
    public void MAPF_AStarVsFocal() {
        var maps = new String[] { "MAPF00", "MAPF01", "MAPF02", "MAPF03" };
        benchmarkMaps(maps);
    }

    @Ignore
    @Test
    public void MAPFreorder_AStarVsFocal() {
        var maps = new String[] { "MAPFreorder_oneagentgoal",  };
        benchmarkMaps(maps);
    }

    @Ignore
    @Test
    public void SAsimple_AStarVsFocal() {
        var maps = new String[] { "SAsimple0", };
        benchmarkMaps(maps);
    }
}
