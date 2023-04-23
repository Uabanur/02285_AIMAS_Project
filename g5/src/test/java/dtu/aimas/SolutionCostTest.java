package dtu.aimas;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dtu.aimas.search.Action;
import dtu.aimas.search.solutions.ActionSolution;
import dtu.aimas.search.solutions.Solution;

public class SolutionCostTest {
    @Test
    public void Goal_State_Solution_Cost_Is_Zero() {
        int emptyPlanLength = 0;
        Action[][] plan = new Action[emptyPlanLength][emptyPlanLength];
        Solution solution = new ActionSolution(plan);

        int expectedFlowtime = 0;
        int expectedMakespan = 0;

        assertEquals(expectedFlowtime, solution.getFlowtime());
        assertEquals(expectedMakespan, solution.getMakespan());
    }
    
    @Test
    public void Inequal_Length_Of_Solutions_For_Agents() {
        Action[][] plan = new Action[][]
        {
            {Action.MoveE, Action.MoveE},
            {Action.MoveE, Action.NoOp}, 
            {Action.MoveE, Action.NoOp}
        };
        Solution solution = new ActionSolution(plan);

        int expectedFlowtime = 4;
        int expectedMakespan = 3;

        assertEquals(expectedFlowtime, solution.getFlowtime());
        assertEquals(expectedMakespan, solution.getMakespan());
    }

    @Test
    public void Equal_Length_Of_Solutions_For_Agents_But_One_Waits() {
        Action[][] plan = new Action[][]
        {
            {Action.MoveE, Action.NoOp}, 
            {Action.MoveE, Action.MoveE}, 
            {Action.MoveE, Action.MoveE}
        };
        Solution solution = new ActionSolution(plan);

        int expectedFlowtime = 6;
        int expectedMakespan = 3;

        assertEquals(expectedFlowtime, solution.getFlowtime());
        assertEquals(expectedMakespan, solution.getMakespan());
    }
}
