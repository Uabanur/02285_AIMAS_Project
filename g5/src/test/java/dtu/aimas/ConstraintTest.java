package dtu.aimas;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Color;
import dtu.aimas.common.Position;
import dtu.aimas.search.solvers.Constraint;

public class ConstraintTest {
    @Test
    public void EmptyConstraint_Should_Not_Reserve() {
        var agent = new Agent(new Position(0, 0), Color.Red, '0');
        var emptyConstraint = Constraint.empty();

        Assert.assertEquals("Empty constraint should not reserve position", 
            0, emptyConstraint.getReserved(agent, 0).size());
    }
    
    @Test
    public void AgentIsConstrained_Should_Reserve() {
        var agent = new Agent(new Position(0, 0), Color.Red, '0');
        
        var constrainedPosition = new Position(2, 2);
        var constrainedTimeStep = 2;
        var constraint = Constraint.empty().extend(agent, constrainedPosition, constrainedTimeStep);
    
        Assert.assertTrue("Expected position reserved", 
            constraint.isReserved(agent, constrainedPosition, constrainedTimeStep));
    }

    @Test
    public void DifferentAgentIsConstrained_Should_Not_Reserve() {
        var agent = new Agent(new Position(0, 0), Color.Red, '0');
        var other = new Agent(new Position(1, 0), Color.Blue, '1');
        
        var constrainedPosition = new Position(2, 2);
        var constrainedTimeStep = 2;
        var constraint = Constraint.empty().extend(other, constrainedPosition, constrainedTimeStep);
    
        Assert.assertFalse("Expected position not reserved", 
            constraint.isReserved(agent, constrainedPosition, constrainedTimeStep));
    }
}
