package dtu.aimas;

import java.util.stream.IntStream;

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

    private void benchmarkMaps(String[] maps, double focalBound){
        Focal focalSolver = new Focal(new DistanceSumCost(), focalBound);
        AStar astarSolver = new AStar(new DistanceSumCost());

        var focalTimes = new long[maps.length];
        var astarTimes = new long[maps.length];

        for(int i=0; i<maps.length; i++){
            var start = Stopwatch.getTimeMs();
            LevelSolver.testMap(maps[i], astarSolver);
            var timeElapsed = Stopwatch.getTimeSinceMs(start);
            astarTimes[i] = timeElapsed;



            start = Stopwatch.getTimeMs();
            LevelSolver.testMap(maps[i], focalSolver);
            timeElapsed = Stopwatch.getTimeSinceMs(start);
            focalTimes[i] = timeElapsed;
        }

        // IO.logLevel = LogLevel.Debug;
        for(int i=0; i<maps.length; i++){
            IO.debug("Map: " + maps[i] + " Focal: " + focalTimes[i] + "[ms] AStar: " + astarTimes[i] + "[ms]");
        }
    }

    private void benchmarkBounds(String mapName, double[] bounds){
        var solvers = new Focal[bounds.length];
        for(int i=0; i<bounds.length; i++){
            solvers[i] = new Focal(new DistanceSumCost(), bounds[i]);
        }
        var times = new long[bounds.length];

        for(int i=0; i<solvers.length; i++){
            var start = Stopwatch.getTimeMs();
            LevelSolver.testMap(mapName, solvers[i]);
            var timeElapsed = Stopwatch.getTimeSinceMs(start);
            times[i] = timeElapsed;
        }

        // IO.logLevel = LogLevel.Debug;
        for(int i=0; i<bounds.length; i++){
            IO.debug("Bound: " + bounds[i] + " Time: " + times[i] + "[ms]");
        }
    }

    @Test
    public void AStarVsFocal_MAPF() {
        var maps = new String[] { "MAPF00", "MAPF01", "MAPF02", "MAPF03" };
        benchmarkMaps(maps, 2.0);
    }

    @Test
    public void AStarVsFocal_SAsimple() {
        var maps = new String[] { "SAsimple0",  "SAsimple1", "SAsimple2", "SAsimple3", "SAsimple4", };
        benchmarkMaps(maps, 2.0);
    }

    @Test
    public void DifferentFocalBounds_SAsoko3_04() {
        var bounds = new double[]{1.2, 1.4, 1.6, 1.8, 2.0};
        benchmarkBounds("SAsoko3_04", bounds);
    }

    @Test
    public void AStarVsFocal_MAsimple() {
        var maps = new String[] { "MAsimple1", "MAsimple2", "MAsimple3", "MAsimple4" };
        benchmarkMaps(maps, 2.0);
    }

    @Test
    public void Look_How_Fast_Focal_Is() {
        var maps = new String[] { "SAsoko3_04", "MAPF03"};
        benchmarkMaps(maps, 2.0);
    }

    // github does not like this test, passes locally
    @Ignore
    @Test
    public void AStarUnfriendly_DifferentFocalBounds() {
        var bounds = new double[]{2.0};
        benchmarkBounds("AStarUnfriendly", bounds);
    }

    // !!WARNING!!
    // astar cant (or can but rather super slow) solve it, so does focal with w<1.5
    @Ignore
    @Test
    public void AStarUnfriendly_AStarVsFocal() {
        var maps = new String[] { "AStarUnfriendly",};
        benchmarkMaps(maps, 2.0);
    }

}
