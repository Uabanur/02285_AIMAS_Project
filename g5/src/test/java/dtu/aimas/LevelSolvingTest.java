package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.helpers.FileHelper;
import dtu.aimas.helpers.LevelSolver;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.search.problems.AgentProblemSplitter;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.problems.RegionProblemSplitter;
import dtu.aimas.search.solvers.blackboard.BlackboardSolver;
import dtu.aimas.search.solvers.graphsearch.*;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.heuristics.GoalCount;
import dtu.aimas.search.solvers.heuristics.MAAdmissibleCost;
import dtu.aimas.search.solvers.heuristics.SingleGoalDistanceCost;
import dtu.aimas.search.solvers.safeinterval.SafePathSolver;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.SAOrderedSolver;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.agent.WalledFinishedBoxes;
import dtu.aimas.search.solvers.conflictbasedsearch.ConflictBasedSearch;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class LevelSolvingTest {

    @Test
    public void TestMAPF00_BFS_TimeLimit_500ms() {
        LevelSolver.testMap("MAPF00", new BFS(), 500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void TestMAPF00_DFS_TimeLimit_500ms() {
        LevelSolver.testMap("MAPF00", new DFS(), 500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void TestMAPF01_BFS_TimeLimit_1Second() {
        LevelSolver.testMap("MAPF01", new BFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF01_DFS_TimeLimit_1Second() {
        LevelSolver.testMap("MAPF01", new DFS(), 1, TimeUnit.SECONDS);
    }
    @Test
    public void TestMAPF00_AStar_GoalCount() {
        LevelSolver.testMap("MAPF00", new AStar(new GoalCount()));
    }

    @Test
    public void TestMAPF00_AStar_DistanceCost() {
        LevelSolver.testMap("MAPF00", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAPF00_Greedy_GoalCount() {
        LevelSolver.testMap("MAPF00", new Greedy(new GoalCount()));
    }

    @Test
    public void TestMAPF01_BFS() {
        LevelSolver.testMap("MAPF01", new BFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF01_DFS() {
        LevelSolver.testMap("MAPF01", new DFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF01_AStar_GoalCount() {
        LevelSolver.testMap("MAPF01", new AStar(new GoalCount()));
    }

    @Test
    public void TestMAPF01_AStar_MAAdmissibleCost() {
        LevelSolver.testMap("MAPF01", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAPF01_Greedy_GoalCount() {
        LevelSolver.testMap("MAPF01", new Greedy(new GoalCount()));
    }

    @Test
    public void TestSAD1_BFS() {
        LevelSolver.testMap("SAD1", new BFS());
    }

    @Test
    public void TestMAPF02_AStar_GoalCount() {
        LevelSolver.testMap("MAPF02", new AStar(new GoalCount()));
    }

    @Test
    public void TestMAPF02_AStar_DistanceCost() {
        LevelSolver.testMap("MAPF02", new AStar(new DistanceSumCost()));
    }
    @Test
    public void TestMAPF02_AStar_MAAdmissibleCost() {
        LevelSolver.testMap("MAPF02", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAsimple4_AStar_DistanceCost() {
        LevelSolver.testMap("MAsimple4", new AStar(new DistanceSumCost()));
    }

    @Test
    public void TestMAsimple4_AStar_MAAdmissibleCost() {
        LevelSolver.testMap("MAsimple4", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAsimple4_AStar_GoalCount() {
        LevelSolver.testMap("MAsimple4", new AStar(new GoalCount()));
    }

    @Test
    public void TestMAsimple5_AStar_DistanceCost() {
        LevelSolver.testMap("MAsimple5", new AStar(new DistanceSumCost()));
    }

    @Test
    public void TestMAsimple5_AStar_MAAdmissibleCost() {
        LevelSolver.testMap("MAsimple5", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAsimple5_AStar_GoalCount() {
        LevelSolver.testMap("MAsimple5", new AStar(new GoalCount()));
    }

    @Ignore
    @Test
    public void TestMishMash_BlackBoard(){
        var solver = new BlackboardSolver(AStarMinLength::new, new DistanceSumCost());
        IO.logLevel = LogLevel.Information;
        LevelSolver.testMap("mishmash", solver);
    }

    @Ignore
    @Test
    public void TestMishMash_SafePath(){
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new RegionProblemSplitter(),
                100
        );

        IO.logLevel = LogLevel.Information;
        LevelSolver.testMap("mishmash", IO.CompLevelDir, solver);
    }

    @Ignore
    @Test
    public void Test_MishMash_R1(){
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                1000
        );

        IO.logLevel = LogLevel.Information;
        LevelSolver.testMap("mishmash_r1", solver);
    }

    @Ignore
    @Test
    public void Test_MishMash_R2(){
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                1000
        );

        IO.logLevel = LogLevel.Information;
        LevelSolver.testMap("mishmash_r2", solver);
    }

    @Ignore
    @Test
    public void Test_MishMash_R3(){
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new AgentProblemSplitter(),
                10000
        );

        IO.logLevel = LogLevel.Information;
        LevelSolver.testMap("mishmash_r3", solver);
    }


    @Ignore
    @Test
    public void Test_MishMash_R4(){
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new AgentProblemSplitter(),
                1000
        );

        IO.logLevel = LogLevel.Information;
        LevelSolver.testMap("mishmash_r4", solver);
    }

    @Ignore
    @Test
    public void Test_Group80(){
        var solver = new SafePathSolver(
                new WalledFinishedBoxes(new AStar(new DistanceSumCost())),
                new AgentProblemSplitter(),
                1000
        );

        LevelSolver.testMap("Group80", solver);
    }

    @Ignore
    @Test
    public void Test_Comp23(){
        var subSolver = new SafePathSolver(
//                new Focal(new DistanceSumCost(), 2.0),
                new AStar(new DistanceSumCost()),
                new AgentProblemSplitter(),
                5
        );

        var solver = new SafePathSolver(
                subSolver,
                new RegionProblemSplitter()
        );

        IO.logLevel = LogLevel.Debug;
        var dir = IO.CompLevelDir;
        for(var level: FileHelper.listDirectory(dir, ".lvl")){
            LevelSolver.testMap(level, dir, solver,
                    5, TimeUnit.SECONDS,
                    CourseLevelParser.Instance, false);
        }
    }
}
