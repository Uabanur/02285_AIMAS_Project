package dtu.aimas;

import dtu.aimas.common.*;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Action;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.blackboard.BlackboardSolver;
import dtu.aimas.search.solvers.graphsearch.AStarMinLength;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.heuristics.GoalCount;
import org.junit.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlackboardSolverTest {
    private final LevelParser levelParser = CourseLevelParser.Instance;
    private Result<Solution> solution;
    private long startTimeMs = 0;

    @Before
    public void setup(){
        IO.logLevel = LogLevel.Information;
        startTimeMs = System.currentTimeMillis();
        solution = Result.error(new SolutionNotFound());
    }

    @After
    public void after(){
        IO.debug("Test time: %d ms", System.currentTimeMillis() - startTimeMs);
        solution.ifOk(s -> {
            IO.debug("Solution of size %d found:\n", s.size());
            s.serializeSteps().forEach(IO::debug);
        });
    }
    
    private Problem getProblem(String level, String... colors){
        var levelWithHeader = String.format("%s\n%s", createLevelHeader(colors), level);
        var parsed = this.levelParser.parse(new StringReader(levelWithHeader));
        Assert.assertTrue(parsed.getErrorMessageOrEmpty(), parsed.isOk());
        return parsed.get();
    }

    private String createLevelHeader(String... colors){
        var colorString = String.join("\n", colors);
        var template = """
                        #domain
                        hospital
                        #levelname
                        test
                        #colors
                        %s
                        """;
    
        return String.format(template, colorString).trim();
    }

    @Test
    public void EmptyProblem() {
        var level = """
                    #initial
                    +++
                    + +
                    +++
                    #goal
                    +++
                    + +
                    +++
                    #end
                    """;

        var problem = getProblem(level);
        var solver = new BlackboardSolver(AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test 
    public void SingleAgent1Box() {
        var level = """
                    #initial
                    +++++
                    +0A +
                    +++++
                    #goal
                    +++++
                    +  A+
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A");
        var solver = new BlackboardSolver(AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void TwoAgentsNoConflict(){
        var level = """
                    #initial
                    +++++
                    +0A +
                    +++++
                    +1B +
                    +++++
                    #goal
                    +++++
                    +  A+
                    +++++
                    +  B+
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A", "blue: 1, B");
        var solver = new BlackboardSolver(AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void TwoAgents_Crossing(){
        var level = """
                    #initial
                    +++++
                    ++0++
                    +1  +
                    ++ ++
                    +++++
                    #goal
                    +++++
                    ++ ++
                    +  1+
                    ++0++
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0", "blue: 1");
        var solver = new BlackboardSolver(AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void MultipleAgentsDifferentColors(){
        var level = """
                #initial
                ++++++++++
                +01A    3+
                +  B     +
                +        +
                +   2C   +
                ++++++++++
                #goal
                ++++++++++
                +       C+
                +        +
                +       A+
                +B      3+
                ++++++++++
                #end
                """;
        var problem = getProblem(level, "red: 0,A", "blue: 1,B", "green: 2,C");
        var solver = new BlackboardSolver(AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void ThreeAgentsCrossing(){
        var level = """
                #initial
                +++++++
                +01   +
                +++ +++
                +  2  +
                +++++++
                #goal
                +++++++
                +    2+
                +++ +++
                +   01+
                +++++++
                #end
                """;
        var problem = getProblem(level, "red: 0,1,2,3,4");
        var solver = new BlackboardSolver(AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.getErrorMessageOrEmpty(), solution.isOk());
    }

    @Test
    public void BlockingFinish(){
        var level = """
                #initial
                +++++++
                +0    +
                +++1+++
                +++++++
                #goal
                +++++++
                +  1 0+
                +++ +++
                +++++++
                #end
                """;
        var problem = getProblem(level, "red: 0, 1");
        var solver = new BlackboardSolver(AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.getErrorMessageOrEmpty(), solution.isOk());
    }

    @Ignore // TODO: for now it cannot manage agents of same color with boxes
    @Test
    public void SameColorAgentsWithBoxes(){
        var level = """
                #initial
                +++++
                +01 +
                +AB +
                +   +
                +++++
                #goal
                +++++
                +   +
                +   +
                +AB +
                +++++
                #end
                """;

        var problem = getProblem(level, "red: 0,1,A,B");
        var solver = new BlackboardSolver(AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }


    @Test
    public void MergeSolutionTest() {
        var solutions = new StateSolution[2];
        { // solution 1
            var states = new State[2];
            var agent0 = new Agent(new Position(0, 0), Color.Blue, '0');
            var box0 = new Box(new Position(agent0.pos.row, agent0.pos.col+1), Color.Blue, 'A');
            var agent1 = new Agent(new Position(2, 0), Color.Red, '2');
            var box1 = new Box(new Position(agent1.pos.row, agent1.pos.col+1), Color.Red, 'C');

            { // step 0
                states[0] = new State(
                    new ArrayList<>(List.of(agent0.clone(), agent1.clone())),
                    new ArrayList<>(List.of(box0.clone(), box1.clone()))
                );
            }
            { // step 1
                // both agents go to the right, pushing the box
                var jointAction = new Action[]{Action.PushEE, Action.PushEE};

                agent0.pos.col += jointAction[0].agentColDelta;
                agent0.pos.row += jointAction[0].agentRowDelta;
                box0.pos.col += jointAction[0].boxColDelta;
                box0.pos.row += jointAction[0].boxRowDelta;

                agent1.pos.col += jointAction[1].agentColDelta;
                agent1.pos.row += jointAction[1].agentRowDelta;
                box1.pos.col += jointAction[1].boxColDelta;
                box1.pos.row += jointAction[1].boxRowDelta;
                states[1] = new State(
                    states[0],
                    new ArrayList<>(List.of(agent0.clone(), agent1.clone())),
                    new ArrayList<>(List.of(box0.clone(), box1.clone())),
                    jointAction
                );
            }
            solutions[0] = new StateSolution(states);
        }

        { // solution 2, longer than solution 1
            var states = new State[3];
            var agent0 = new Agent(new Position(4, 3), Color.Green, '1');
            var box0 = new Box(new Position(agent0.pos.row, agent0.pos.col+1), Color.Green, 'B');
            var agent1 = new Agent(new Position(5, 3), Color.Grey, '3');
            var box1 = new Box(new Position(agent1.pos.row, agent1.pos.col+1), Color.Grey, 'D');

            { // step 0
                states[0] = new State(
                        new ArrayList<>(List.of(agent0.clone(), agent1.clone())),
                        new ArrayList<>(List.of(box0.clone(), box1.clone()))
                );
            }
            { // step 1
                // agent0 does nothing, agent1 pushes right
                var jointAction = new Action[]{Action.NoOp, Action.PushEE};

                agent0.pos.col += jointAction[0].agentColDelta;
                agent0.pos.row += jointAction[0].agentRowDelta;
                box0.pos.col += jointAction[0].boxColDelta;
                box0.pos.row += jointAction[0].boxRowDelta;

                agent1.pos.col += jointAction[1].agentColDelta;
                agent1.pos.row += jointAction[1].agentRowDelta;
                box1.pos.col += jointAction[1].boxColDelta;
                box1.pos.row += jointAction[1].boxRowDelta;
                states[1] = new State(
                        states[0],
                        new ArrayList<>(List.of(agent0.clone(), agent1.clone())),
                        new ArrayList<>(List.of(box0.clone(), box1.clone())),
                        jointAction
                );
            }
            { // step 2
                // agent0 pushes right, agent1 goes left
                var jointAction = new Action[]{Action.PushEE, Action.MoveW};

                agent0.pos.col += jointAction[0].agentColDelta;
                agent0.pos.row += jointAction[0].agentRowDelta;
                box0.pos.col += jointAction[0].boxColDelta;
                box0.pos.row += jointAction[0].boxRowDelta;

                agent1.pos.col += jointAction[1].agentColDelta;
                agent1.pos.row += jointAction[1].agentRowDelta;
                box1.pos.col += jointAction[1].boxColDelta;
                box1.pos.row += jointAction[1].boxRowDelta;
                states[2] = new State(
                        states[0],
                        new ArrayList<>(List.of(agent0.clone(), agent1.clone())),
                        new ArrayList<>(List.of(box0.clone(), box1.clone())),
                        jointAction
                );
            }
            solutions[1] = new StateSolution(states);
        }

        var result = SolutionMerger.mergeSolutions(List.of(solutions));
        Assert.assertEquals(3, result.size());
        { // step 0
            var state = result.getState(0);
            var agent0 = new Agent(new Position(0, 0), Color.Blue, '0');
            var box0 = new Box(new Position(0, 1), Color.Blue, 'A');

            var agent1 = new Agent(new Position(4, 3), Color.Green, '1');
            var box1 = new Box(new Position(4, 4), Color.Green, 'B');

            var agent2 = new Agent(new Position(2, 0), Color.Red, '2');
            var box2 = new Box(new Position(2, 1), Color.Red, 'C');

            var agent3 = new Agent(new Position(5, 3), Color.Grey, '3');
            var box3 = new Box(new Position(5, 4), Color.Grey, 'D');

            // require order of agents
            var expectedAgents = new ArrayList<>(List.of(agent0, agent1, agent2, agent3));
            Assert.assertEquals(expectedAgents, state.agents);

            // boxes can have any order, but all must be there
            Assert.assertEquals(4, state.boxes.size());
            for (var box : List.of(box0, box1, box2, box3)) {
                Assert.assertTrue(state.boxes.contains(box));
            }

            // Step 0 has no joint action
            Assert.assertNull(state.jointAction);
        }

        { // step 1
            var state = result.getState(1);

            // PushEE
            var agent0 = new Agent(new Position(0, 1), Color.Blue, '0');
            var box0 = new Box(new Position(0, 2), Color.Blue, 'A');

            // NoOp
            var agent1 = new Agent(new Position(4, 3), Color.Green, '1');
            var box1 = new Box(new Position(4, 4), Color.Green, 'B');

            // PushEE
            var agent2 = new Agent(new Position(2, 1), Color.Red, '2');
            var box2 = new Box(new Position(2, 2), Color.Red, 'C');

            // PushEE
            var agent3 = new Agent(new Position(5, 3+1), Color.Grey, '3');
            var box3 = new Box(new Position(5, 4+1), Color.Grey, 'D');

            // require order of agents
            var expectedAgents = new ArrayList<>(List.of(agent0, agent1, agent2, agent3));
            Assert.assertEquals(expectedAgents, state.agents);

            // boxes can have any order, but all must be there
            Assert.assertEquals(4, state.boxes.size());
            for (var box : List.of(box0, box1, box2, box3)) {
                Assert.assertTrue(state.boxes.contains(box));
            }

            // require order of joint action
            var expectedJointAction = new Action[]{Action.PushEE, Action.NoOp, Action.PushEE, Action.PushEE};
            Assert.assertArrayEquals(expectedJointAction, state.jointAction);
        }


        { // step 2
            var state = result.getState(2);

            // NoOp
            var agent0 = new Agent(new Position(0, 1), Color.Blue, '0');
            var box0 = new Box(new Position(0, 1 + 1), Color.Blue, 'A');

            // PushEE
            var agent1 = new Agent(new Position(4, 3 + 1), Color.Green, '1');
            var box1 = new Box(new Position(4, 4 + 1), Color.Green, 'B');

            // NoOp
            var agent2 = new Agent(new Position(2, 1), Color.Red, '2');
            var box2 = new Box(new Position(2, 1 + 1), Color.Red, 'C');

            // MoveW
            var agent3 = new Agent(new Position(5, 3+1-1), Color.Grey, '3');
            var box3 = new Box(new Position(5, 4 + 1), Color.Grey, 'D');

            // require order of agents
            var expectedAgents = new ArrayList<>(List.of(agent0, agent1, agent2, agent3));
            Assert.assertEquals(expectedAgents, state.agents);

            // boxes can have any order, but all must be there
            Assert.assertEquals(4, state.boxes.size());
            for (var box : List.of(box0, box1, box2, box3)) {
                Assert.assertTrue(state.boxes.contains(box));
            }

            // require order of joint action
            var expectedJointAction = new Action[]{Action.NoOp, Action.PushEE, Action.NoOp, Action.MoveW};
            Assert.assertArrayEquals(expectedJointAction, state.jointAction);
        }
    }

    @Test
    public void CombinationsTest(){
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

        var solver = new BlackboardSolver(AStarMinLength::new, new GoalCount());
        var result = solver.combinations(options, freezePosition, freezeValue);
        Assert.assertEquals(expected.size(), result.size());
        for(var permutation: expected){
            Assert.assertTrue(result.stream().anyMatch(p -> Arrays.equals(p, permutation)));
        }
    }
}
