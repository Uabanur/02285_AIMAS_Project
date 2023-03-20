package dtu.aimas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.BFS;


public class LevelTester {
    private Result<Reader> getFileReader(String levelName){
        try {
            var levelFile = new File(IO.LevelDir.toFile(), levelName + ".lvl");
            var buffer = new FileReader(levelFile);
            return Result.ok(buffer);
        } catch (FileNotFoundException e) {
            return Result.error(e);
        }
    }

    private Result<Problem> LoadLevel(String levelName) {
        return getFileReader(levelName).flatMap(lvl -> parser.parse(lvl));
    }

    private void TestMap(String levelName, Solver solver)
    {
        if (logOutputToFile) IO.logOutputToFile(solver.getClass().getSimpleName() + "_" + levelName);
        var solution = LoadLevel(levelName).flatMap(solver::solve);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    static final boolean logOutputToFile = true;
    static final LevelParser parser = CourseLevelParser.Instance;
    
    @Test
    public void TestMAPF00_BFS() {
        TestMap("MAPF00", new BFS());
    }

    @Test
    public void TestPrecalcMAPF00() {
        var problem = LoadLevel("MAPF00").get().precompute();
        Assert.assertEquals(problem.admissibleDist(new Position(1,1), new Position(1,1)), 0);
        Assert.assertEquals(problem.admissibleDist(new Position(1,1), new Position(5,11)), 14);
        Assert.assertEquals(problem.admissibleDist(new Position(5,11), new Position(1,1)), 14);
        Assert.assertEquals(problem.admissibleDist(new Position(1,1), new Position(5,12)), Integer.MAX_VALUE);
        Assert.assertEquals(problem.admissibleDist(new Position(1,11), new Position(3,11)), 8);
        Assert.assertEquals(problem.admissibleDist(new Position(5,11), new Position(5,9)), 8);
        Assert.assertEquals(problem.admissibleDist(new Position(0,0), new Position(1,1)), Integer.MAX_VALUE);
        Assert.assertEquals(problem.admissibleDist(new Position(0,0), new Position(6,12)), Integer.MAX_VALUE);
    }

    @Test
    public void TestPrecalcMASeparate() {
        var problem = LoadLevel("MASeparate").get().precompute();
        Assert.assertEquals(problem.admissibleDist(new Position(1,1), new Position(1,7)), Integer.MAX_VALUE);
        Assert.assertEquals(problem.admissibleDist(new Position(1,1), new Position(3,10)), Integer.MAX_VALUE);
        Assert.assertEquals(problem.admissibleDist(new Position(3,10), new Position(1,1)), Integer.MAX_VALUE);
        Assert.assertEquals(problem.admissibleDist(new Position(1,1), new Position(5,1)), 4);
        Assert.assertEquals(problem.admissibleDist(new Position(5,10), new Position(4,10)), Integer.MAX_VALUE);
        Assert.assertEquals(problem.admissibleDist(new Position(5,10), new Position(3,10)), 6);
        Assert.assertEquals(problem.admissibleDist(new Position(5,10), new Position(1,7)), 15);
    }
}