package dtu.aimas;

import org.junit.Test;

import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;

import java.util.List;

import org.junit.Assert;

public class ProblemPositionCheckTests {
    @Test
    public void isChokepointTest() {
        int width = 10;
        int height = 10;
        boolean walls[][] = new boolean[height][width];
        char goals[][] = new char[height][width];
        //small horizontal and vertical corridor
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++) {
                if(i == 5 || j == 5 || i == 0 || i == height-1 || j == 0 || j == width-1)
                    walls[i][j] = false;
                else 
                    walls[i][j] = true;
            }
        }
        //small hole in one of the corners
        walls[2][2] = false; walls[2][3] = false;
        walls[3][2] = false; walls[3][3] = false;
        Problem problem = new Problem(null, null, walls, goals);
        Assert.assertEquals(true, problem.isChokepoint(new Position(5,3)));
        Assert.assertEquals(true, problem.isChokepoint(new Position(2,5)));
        Assert.assertEquals(true, problem.isChokepoint(new Position(5,5)));
        Assert.assertEquals(true, problem.isChokepoint(new Position(0,0)));
        Assert.assertEquals(true, problem.isChokepoint(new Position(9,9)));
        Assert.assertEquals(true, problem.isChokepoint(new Position(0,6)));
        Assert.assertEquals(true, problem.isChokepoint(new Position(6,0)));

        Assert.assertEquals(false, problem.isChokepoint(new Position(2,2)));
    }

    public void isDeadEndTest() {
        boolean x = true; boolean o = false;
        boolean walls[][] = new boolean[][] {
            new boolean[] {x, x, x, x, x, x, x},
            new boolean[] {x, o, x, o, x, o, x},
            new boolean[] {x, x, x, o, x, o, x},
            new boolean[] {x, o, o, o, o, o, o},
            new boolean[] {x, x, x, x, x, x, x},
        };
        char goals[][] = new char[walls.length][walls[0].length];
        var problem = new Problem(null, null, walls, goals);

        Assert.assertEquals(true, problem.isDeadEnd(new Position(1,1)));
        Assert.assertEquals(true, problem.isDeadEnd(new Position(1,3)));
        Assert.assertEquals(true, problem.isDeadEnd(new Position(3,1)));
        Assert.assertEquals(true, problem.isDeadEnd(new Position(3,6)));

        Assert.assertEquals(false, problem.isDeadEnd(new Position(3,2)));
        Assert.assertEquals(false, problem.isDeadEnd(new Position(2,3)));
        Assert.assertEquals(false, problem.isDeadEnd(new Position(3,3)));
        Assert.assertEquals(false, problem.isDeadEnd(new Position(3,5)));
    }
}
