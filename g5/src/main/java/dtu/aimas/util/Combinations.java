package dtu.aimas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Combinations {
    public static List<int[]> get(int[] options, int freezePosition, int freezeValue){
        var result = new ArrayList<int[]>();

        var permutation = new int[options.length];
        permutation[freezePosition] = freezeValue;
        var mutableOptions = IntStream.range(0, options.length)
                .filter(i -> i != freezePosition).toArray();

        while(true){
            result.add(permutation.clone());
            for(var i: mutableOptions){
                if(permutation[i] < options[i] - 1){
                    ++permutation[i];
                    break;
                }
                else{
                    permutation[i] = 0;
                    if(i == mutableOptions[mutableOptions.length-1]){
                        return result;
                    }
                }
            }
        }
    }
}
