package dtu.aimas;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Color;
import dtu.aimas.common.Position;
import dtu.aimas.search.Action;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.SolutionMerger;
import dtu.aimas.search.solvers.graphsearch.State;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SolutionMergeTest {
    @Test
    public void MergeSolutionTest() {
        var solutions = new StateSolution[2];
        { // solution 1
            var states = new State[2];
            var agent0 = new Agent(new Position(0, 0), Color.Blue, '0');
            var box0 = new Box(new Position(agent0.pos.row, agent0.pos.col+1), Color.Blue, 'A', 0);
            var agent1 = new Agent(new Position(2, 0), Color.Red, '2');
            var box1 = new Box(new Position(agent1.pos.row, agent1.pos.col+1), Color.Red, 'C', 2);

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
            var box0 = new Box(new Position(agent0.pos.row, agent0.pos.col+1), Color.Green, 'B', 1);
            var agent1 = new Agent(new Position(5, 3), Color.Grey, '3');
            var box1 = new Box(new Position(agent1.pos.row, agent1.pos.col+1), Color.Grey, 'D', 3);

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
            var box0 = new Box(new Position(0, 1), Color.Blue, 'A', 0);

            var agent1 = new Agent(new Position(4, 3), Color.Green, '1');
            var box1 = new Box(new Position(4, 4), Color.Green, 'B', 1);

            var agent2 = new Agent(new Position(2, 0), Color.Red, '2');
            var box2 = new Box(new Position(2, 1), Color.Red, 'C', 2);

            var agent3 = new Agent(new Position(5, 3), Color.Grey, '3');
            var box3 = new Box(new Position(5, 4), Color.Grey, 'D', 3);

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
            var box0 = new Box(new Position(0, 2), Color.Blue, 'A', 0);

            // NoOp
            var agent1 = new Agent(new Position(4, 3), Color.Green, '1');
            var box1 = new Box(new Position(4, 4), Color.Green, 'B', 1);

            // PushEE
            var agent2 = new Agent(new Position(2, 1), Color.Red, '2');
            var box2 = new Box(new Position(2, 2), Color.Red, 'C', 2);

            // PushEE
            var agent3 = new Agent(new Position(5, 3+1), Color.Grey, '3');
            var box3 = new Box(new Position(5, 4+1), Color.Grey, 'D', 3);

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
            var box0 = new Box(new Position(0, 1 + 1), Color.Blue, 'A', 0);

            // PushEE
            var agent1 = new Agent(new Position(4, 3 + 1), Color.Green, '1');
            var box1 = new Box(new Position(4, 4 + 1), Color.Green, 'B', 1);

            // NoOp
            var agent2 = new Agent(new Position(2, 1), Color.Red, '2');
            var box2 = new Box(new Position(2, 1 + 1), Color.Red, 'C', 2);

            // MoveW
            var agent3 = new Agent(new Position(5, 3+1-1), Color.Grey, '3');
            var box3 = new Box(new Position(5, 4 + 1), Color.Grey, 'D', 3);

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
}
