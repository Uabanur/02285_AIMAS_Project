package dtu.aimas;

import dtu.aimas.util.Combinations;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationsTest {
    @Test
    public void test(){
        var freezePosition = 1;
        var freezeValue = 5;
        var options = new int[]{3, 2, 4};
        var expected = new ArrayList<>(List.of(
                new int[]{0, 5, 0},
                new int[]{0, 5, 1},
                new int[]{0, 5, 2},
                new int[]{0, 5, 3},
                new int[]{1, 5, 0},
                new int[]{1, 5, 1},
                new int[]{1, 5, 2},
                new int[]{1, 5, 3},
                new int[]{2, 5, 0},
                new int[]{2, 5, 1},
                new int[]{2, 5, 2},
                new int[]{2, 5, 3}
        ));

        var result = Combinations.get(options, freezePosition, freezeValue);
        Assert.assertEquals(expected.size(), result.size());
        for(var permutation: expected){
            Assert.assertTrue(result.stream().anyMatch(p -> Arrays.equals(p, permutation)));
        }
    }
}
