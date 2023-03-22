package dtu.aimas.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Color;
import dtu.aimas.common.Map;
import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;

public class CourseLevelParser extends LevelParser
{
    private CourseLevelParser(){}
    public static final LevelParser Instance = new CourseLevelParser();

    protected Result<Problem> parseInput(Reader level) 
    {
        try {
            return Result.ok(parseUnsafe(new BufferedReader(level)));
        } catch (Throwable e) {
            return Result.error(e);
        }
    }

    private static Problem parseUnsafe(BufferedReader level) throws IOException
    {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        
        level.readLine(); // #domain
        level.readLine(); // hospital

        // Read Level name
        level.readLine(); // #levelname
        level.readLine(); // <name>

        // Read colors
        level.readLine(); // #colors

        var agentColors = new Color[10];
        var boxColors = new Color[26];
        var line = level.readLine();


        while (!line.startsWith("#"))
        {
            var split = line.split(":");
            var color = Color.fromString(split[0].strip());
            var entities = split[1].split(",");
            for (var entity : entities)
            {
                char c = entity.strip().charAt(0);
                if(Agent.isLabel(c))
                {
                    agentColors[c - '0'] = color;
                }
                else if (Box.isLabel(c))
                {
                    boxColors[c - 'A'] = color;
                }
            }
            line = level.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        var levelLines = new ArrayList<String>(64);
        line = level.readLine();
        while (!line.startsWith("#"))
            {
                levelLines.add(line);
                numCols = Math.max(numCols, line.length());
                ++numRows;
                line = level.readLine();
        }
        var numAgents = 0;
        var agentRows = new int[10];
        var agentCols = new int[10];
        var walls = new boolean[numRows][numCols];
        var boxes = new char[numRows][numCols];
        for (int row = 0; row < numRows; ++row)
        {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if (Agent.isLabel(c))
                {
                    agentRows[c - '0'] = row;
                    agentCols[c - '0'] = col;
                    ++numAgents;
                }
                else if (Box.isLabel(c))
                {
                    boxes[row][col] = c;
                }
                else if (Map.isWall(c))
                {
                    walls[row][col] = true;
                }
            }
        }

        agentRows = Arrays.copyOf(agentRows, numAgents);
        agentCols= Arrays.copyOf(agentCols, numAgents);

        // Read goal state
        // line is currently "#goal"
        var goals = new char[numRows][numCols];
        line = level.readLine();
        var row = 0;
        while (!line.startsWith("#"))
        {
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if (Agent.isLabel(c) || Box.isLabel(c))
                {
                    goals[row][col] = c;
                }
            }

            ++row;
            line = level.readLine();
        }

        // End
        // line is currently "#end"
        assert line.equals("#end");


        // return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
        return mapToProblem(agentRows, agentCols, agentColors, boxColors, boxes, goals, walls);
    }

    private static Problem mapToProblem(
        int[] agentRows, int[] agentCols, Color[] agentColors, 
        Color[] boxColors, char[][] boxes, 
        char[][] goals, boolean[][] walls)
    {
        var agentList = IntStream.range(0, agentRows.length)
            .mapToObj(i -> new Agent(new Position(agentRows[i], agentCols[i]), agentColors[i]))
            .collect(Collectors.toList());

        var boxList = new ArrayList<Box>();
        for(var row = 0; row < boxes.length; row++){
            for(var col = 0; col < boxes[row].length; col++){
                var c = boxes[row][col];
                if(!Box.isLabel(c)) continue;
                boxList.add(new Box(new Position(row, col), boxColors[c-'A'], c));
            }
        }

        return new Problem(agentList, boxList, walls, goals);
    }
}
