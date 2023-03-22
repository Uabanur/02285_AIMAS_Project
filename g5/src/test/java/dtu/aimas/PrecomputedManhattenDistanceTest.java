package dtu.aimas;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;

public class PrecomputedManhattenDistanceTest {
    private static final Collection<Agent> NoAgents = new ArrayList<>();
    private static final Collection<Box> NoBoxes = new ArrayList<>();
    private int manhattenDistance(Position from, Position to){
        return Math.abs(from.row - to.row) + Math.abs(from.col - to.col);
    }

    @Test
    public void ManhattenDistance_NoWalls() {
        var width = 20; 
        var height = 10;
        var goals = new char[height][width];
        var walls = new boolean[height][width];

        var problem = new Problem(NoAgents, NoBoxes, walls, goals).precompute();

        var tests = new Position[][]{
            // same place
            new Position[] {new Position(1, 1), new Position(1, 1)},

            // orthogonal difference
            new Position[] {new Position(1, 1), new Position(1, width-1)},
            new Position[] {new Position(1, 1), new Position(height-1, 1)},

            // diagonal difference
            new Position[] {new Position(1, 1), new Position(height-1, width-1)},
            new Position[] {new Position(1, width-1), new Position(height-1, 1)},
        };

        for(var test : tests){
            var from = test[0];
            var to = test[1];
            int expected = manhattenDistance(from, to);
            Assert.assertEquals(expected, problem.admissibleDist(from, to));
        }
    }

    @Test
    public void ManhattenDistance_WithWalls_Reachable() {
        var width = 20; 
        var height = 10;
        var goals = new char[height][width];
        var walls = new boolean[height][width];

        // vertical wall in the middle of the map going halfway down
        var wallHeight = height/2;
        for(var row = 0; row < wallHeight; row++) walls[row][width/2] = true;


        var problem = new Problem(NoAgents, NoBoxes, walls, goals).precompute();

        { // Same side. Left of wall
            var col = 1;
            var from = new Position(1, col);
            var to = new Position(height-1, col);
            var expected = manhattenDistance(from, to);
            Assert.assertEquals(expected, problem.admissibleDist(from, to));
        }

        { // Same side. Right of wall
            var col = width-1;
            var from = new Position(1, col);
            var to = new Position(height-1, col);
            var expected = manhattenDistance(from, to);
            Assert.assertEquals(expected, problem.admissibleDist(from, to));
        }

        { // Each side of wall but not separated
            var row = height-1;
            var from = new Position(row, 1);
            var to = new Position(row, width-1);
            var expected = manhattenDistance(from, to);
            Assert.assertEquals(expected, problem.admissibleDist(from, to));
        }

        { // Separated by wall
            var row = 0;
            var from = new Position(row, 1);
            var to = new Position(row, width-1);
            
            var expected = 0;
            // expected manhatten distance: 
            // 1. go down to clear the wall
            expected += wallHeight - from.row;
            // 2. go to the goal column
            expected += Math.abs(from.col - to.col);
            // 3. go to goal row
            expected += wallHeight - to.row;

            Assert.assertEquals(expected, problem.admissibleDist(from, to));
        }
    }

    @Test
    public void ManhattenDistance_WithWalls_NotReachable() {
        var width = 20; 
        var height = 10;
        var goals = new char[height][width];
        var walls = new boolean[height][width];

        // vertical wall in the middle of the map dividing the two halfs
        var wallHeight = height;
        for(var row = 0; row < wallHeight; row++) walls[row][width/2] = true;

        var problem = new Problem(NoAgents, NoBoxes, walls, goals).precompute();

        var row = height/2;
        var from = new Position(row, 1);
        var to = new Position(row, width-1);
        var expected = Integer.MAX_VALUE;
        Assert.assertEquals(expected, problem.admissibleDist(from, to));
    }
}









