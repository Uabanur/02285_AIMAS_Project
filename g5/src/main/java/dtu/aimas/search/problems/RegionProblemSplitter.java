package dtu.aimas.search.problems;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RegionProblemSplitter implements ProblemSplitter {
    private static final char WALL = 255;
    @Override
    public List<Problem> split(Problem problem) {
        var height = problem.goals.length;
        var width = problem.goals[0].length;

        var problems = new ArrayList<Problem>();
        final char[][] world = new char[height][width];
        fillWalls(problem, height, width, world);

        var orphanedBoxes = filterOrphanedBoxes(problem.agents, problem.boxes);
        if(!orphanedBoxes.isEmpty()){
            var orphanedBoxProblem = createGoallessProblem(orphanedBoxes, problem);
            problems.add(orphanedBoxProblem);
            for(var orphanedBox: orphanedBoxes){
                world[orphanedBox.pos.row][orphanedBox.pos.col] = WALL;
            }
        }

        var regions = fillRegions(height, width, world);

        for(char region = 1; region <= regions; region++){
            final char regionId = region;
            var agents = problem.agents.stream().filter(a -> world[a.pos.row][a.pos.col] == regionId).toList();
            var boxes = problem.boxes.stream().filter(b -> world[b.pos.row][b.pos.col] == regionId).toList();
            var goals = extractRegionGoals(problem, height, width, world, regionId);
            var walls = extractRegionWalls(height, width, world, regionId);
            var orphanedRegionBoxes = filterOrphanedBoxes(agents, boxes);
            for(var orphanedBox: orphanedRegionBoxes){
                walls[orphanedBox.pos.row][orphanedBox.pos.col] = true;
            }

            problems.add(problem.copyWith(agents, boxes, goals, walls));
        }

        return problems;
    }

    private Problem createGoallessProblem(List<Box> orphanedBoxes, Problem problem) {
        var goals = new char[problem.goals.length][problem.goals[0].length];
        return problem.copyWith(List.of(), orphanedBoxes, goals);
    }

    private List<Box> filterOrphanedBoxes(Collection<Agent> agents, Collection<Box> boxes) {
        var colors = agents.stream().map(a -> a.color).collect(Collectors.toSet());
        return boxes.stream().filter(b -> !colors.contains(b.color)).toList();
    }

    private static boolean[][] extractRegionWalls(int height, int width, char[][] world, char regionId) {
        var walls = new boolean[height][width];
        for(var row = 0; row < height; row++){
            for(var col = 0; col < width; col++) {
                if(world[row][col] != regionId) {
                    walls[row][col] = true;
                }
            }
        }
        return walls;
    }

    private static char[][] extractRegionGoals(Problem problem, int height, int width, char[][] world, char regionId) {
        var goals = new char[height][width];
        for(var row = 0; row < height; row++){
            for(var col = 0; col < width; col++) {
                if(world[row][col] == regionId){
                    goals[row][col] = problem.goals[row][col];
                }
            }
        }
        return goals;
    }

    private static char fillRegions(int height, int width, char[][] world) {
        char regionId = 0;
        for(var row = 0; row < height; row++){
            for(var col = 0; col < width; col++){
                if(world[row][col] != 0) continue;
                floodRegion(world, ++regionId, row, col);
            }
        }
        return regionId;
    }

    private static void fillWalls(Problem problem, int height, int width, char[][] world) {
        for(var row = 0; row < height; row++){
            for(var col = 0; col < width; col++) {
                if(problem.walls[row][col])
                    world[row][col] = WALL;
            }
        }
    }

    private static void floodRegion(char[][] world, char regionId, int row, int col) {
        var queue = new ArrayDeque<Position>();
        queue.add(new Position(row, col));

        while(!queue.isEmpty()){
            var pos = queue.poll();
            world[pos.row][pos.col] = regionId;
            addToQueueIfEmpty(pos.row+1, pos.col, world, queue);
            addToQueueIfEmpty(pos.row-1, pos.col, world, queue);
            addToQueueIfEmpty(pos.row, pos.col+1, world, queue);
            addToQueueIfEmpty(pos.row, pos.col-1, world, queue);
        }
    }

    private static void addToQueueIfEmpty(int row, int col, char[][] world, ArrayDeque<Position> queue){
        if(row < 0 || row >= world.length) return;
        if(col < 0 || col >= world[0].length) return;
        if(world[row][col] == 0) queue.add(new Position(row, col));
    }
}
