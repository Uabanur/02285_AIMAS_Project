package dtu.aimas.search.solvers.graphsearch;

import java.lang.reflect.Array;
import java.rmi.server.RemoteStub;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Goal;
import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.errors.InvalidOperation;
import dtu.aimas.search.Action;
import dtu.aimas.search.ActionSolution;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import lombok.Getter;

public class StateSpace {
    @Getter
    private Problem problem;

    @Getter
    private State initialState;

    public StateSpace(Problem problem, State initialState) {
        this.problem = problem;
        this.initialState = initialState;
    }

    public Result<Solution> createSolution(State state){
        if (!isGoalState(state)) 
            return Result.error(new InvalidOperation("Can only create a solution from a goal state"));

        return Result.ok(new ActionSolution(state.extractPlan()));
    }

    public boolean isGoalState(State state) {
        // TODO : change this to use the problem from the state space
        return state.isGoalState();
    }

    public ArrayList<State> expand(State state) {
        // TODO : this functionality belongs to the state space
        return state.getExpandedStates();
    }

    public Optional<Agent> getAgentByNumber(State state, int i) {
        if (i >= state.agentRows.length) return Optional.empty();
        return Optional.of(
                new Agent(new Position(state.agentRows[i], state.agentCols[i]), State.agentColors[i]));
    }

    public Optional<Box> getBoxAt(State state, int row, int col) {
        var symbol = state.boxes[row][col];
        if (!Box.isLabel(symbol)) return Optional.empty();
        var color = State.boxColors[symbol-'A'];
        return Optional.of(new Box(new Position(row, col), color, symbol));
    }

    // WIP
    
    // to delete
    private static final Random RNG = new Random(1);
    //

    @Getter
    private LiteState initialLiteState;

    public StateSpace(Problem problem, LiteState initialState) {
        this.problem = problem;
        this.initialLiteState = initialState;
    }

    public Agent getAgentByNumber(LiteState state, int i) {
        return state.agents.get(i);
    }

    public Optional<Box> getBoxAt(LiteState state, Position position) {
        for(Box box : state.boxes){
            if (box.pos == position)
                return Optional.of(box);
        }
        return Optional.empty();
    }

    public Optional<Agent> getAgentAt(LiteState state, Position position){
        for(Agent agent : state.agents){
            if (agent.pos == position)
                return Optional.of(agent);
        }
        return Optional.empty();
    }

    private boolean isWallAt(Position position){
        return this.problem.walls[position.row][position.col];
    }

    private boolean isCellFree(Position position, LiteState state){
        return !isWallAt(position) || !getAgentAt(state, position).isPresent() || !getBoxAt(state, position).isPresent();
    }

    private boolean canMoveBox(Agent agent, Box box){
        return agent.color == box.color;
    }

    public boolean isGoalState(LiteState state) {
        // are agents on their goals?
        for(Goal goal : this.problem.agentGoals){
            var agent = getAgentByNumber(state, goal.label);
            if(!goal.isSatisfied(agent)){
                return false;
            }
        }
        // is each box goal satisfied?
        for(Goal goal : this.problem.boxGoals) {
            boolean goalSatisfied = false;
            for(Box box : state.boxes){
                if(!goal.isSatisfied(box)){
                    goalSatisfied = true;
                    break;
                }
            }
            if(!goalSatisfied){
                return false;
            }
        }
        // everything is satisfied
        return true;
    }

    private boolean isApplicable(Agent agent, Action action){
        LiteState currentState = null;
        Position agentDestination;
        Optional<Box> boxResult;
        Box box;
        // incompatible with applying of joint actions
        switch(action.type){
            case NoOp:
                return true;

            case Move:
                var destination = agent.pos.movePosition(action.agentRowDelta, action.agentColDelta);
                return isCellFree(destination, currentState);

            case Push:
                agentDestination = agent.pos.movePosition(action.agentRowDelta, action.agentColDelta);
                boxResult = getBoxAt(currentState, agentDestination);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false;
                Position boxDestination = box.pos.movePosition(action.boxRowDelta, action.boxColDelta);
                return isCellFree(boxDestination, currentState);

            case Pull:
                Position boxSource = agent.pos.pullPosition(action.boxRowDelta, action.boxColDelta);
                boxResult = getBoxAt(currentState, boxSource);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false; 
                agentDestination = agent.pos.movePosition(action.agentRowDelta, action.agentColDelta);
                return isCellFree(agentDestination, currentState);

        }
        // unreachable
        return false;
    }

    public ArrayList<LiteState> expand(LiteState state) {
        int agentsCount = state.agents.size();
        Action[][] applicableActions = new Action[agentsCount][];
        for(int agentId = 0; agentId < agentsCount; agentId++)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for(Action action : Action.values()){
                if(isApplicable(getAgentByNumber(state, agentId), action)){
                    agentActions.add(action);
                }
            }
            applicableActions[agentId] = agentActions.toArray(new Action[0]);
        }

        // permutations generation, literally copied from warmup
        Action[] jointAction = new Action[agentsCount];
        int[] actionsPermutation = new int[agentsCount];
        ArrayList<LiteState> expandedStates = new ArrayList<>(16);
        while(true){
            for(int i=0; i<agentsCount; i++){
                jointAction[i] = applicableActions[i][actionsPermutation[i]];
            }

            var stateResult = tryCreateState(state, jointAction);
            if(stateResult.isPresent()){
                expandedStates.add(stateResult.get())
            }

            boolean done = false;

            for(int i = 0; i<agentsCount; i++){
                if(actionsPermutation[i] < applicableActions[i].length - 1){
                    ++actionsPermutation[i];
                    break;
                }
                else{
                    actionsPermutation[i] = 0;
                    if(i == agentsCount - 1){
                        done = true;
                    }
                }
            }

            if(done) break;
        }

        Collections.shuffle(expandedStates, StateSpace.RNG);
        return expandedStates;
    }

    private boolean isValid(LiteState state){
        Set<Position> occupiedPositions = new HashSet<>();
        for (Agent agent : state.agents){
            if(!occupiedPositions.add(agent.pos)) return false;
        }
        for (Box box : state.boxes){
            if(!occupiedPositions.add(box.pos)) return false;
        }
        return true;
    }

    private LiteState applyJointActions(LiteState state, Action[] actionsToApply){
        // implementation here
        ArrayList<Agent> destinationAgents = new ArrayList<>(state.agents.size());
        ArrayList<Box> destinationBoxes = new ArrayList<>(state.boxes.size());
        for(Box box : state.boxes){
            destinationBoxes.add(new Box(box.pos, box.color, box.type));
        }
        for(int action = 0; action < actionsToApply.length; action++){
            Agent agent = getAgentByNumber(state, action);
            Position destAgentPosition = (Position) agent.pos.clone();
            Position destBoxPosition;
            Box box;
            Optional<Box> boxResult;
            switch (actionsToApply[action].type)
            {
                case NoOp:
                    break;
                
                case Move:
                    destAgentPosition.updateBy(actionsToApply[action].agentRowDelta, actionsToApply[action].agentColDelta);
                    break;

                case Push:
                    destAgentPosition.updateBy(actionsToApply[action].agentRowDelta, actionsToApply[action].agentColDelta);
                    boxResult = getBoxAt(state, destAgentPosition);
                    box = boxResult.get();
                    destBoxPosition = (Position) box.pos.clone();
                    destBoxPosition.updateBy(actionsToApply[action].boxRowDelta, actionsToApply[action].boxColDelta);
                    break;

                case Pull:
                    boxResult = getBoxAt(state, destAgentPosition.pullPosition(actionsToApply[action].boxRowDelta, actionsToApply[action].boxColDelta));
                    box = boxResult.get();
                    destBoxPosition = (Position) box.pos.clone();
                    destBoxPosition.updateBy(actionsToApply[action].boxRowDelta, actionsToApply[action].boxColDelta);
                    destAgentPosition.updateBy(actionsToApply[action].agentRowDelta, actionsToApply[action].agentColDelta);
                    break;
            }
            destinationAgents.add(new Agent(destAgentPosition, agent.color));
            // add boxes
        }
        return state;
    }

    private Optional<LiteState> tryCreateState(LiteState state, Action[] jointAction){
        LiteState destinationState = applyJointActions(state, jointAction);
        return isValid(destinationState) ? Optional.of(destinationState) : Optional.empty();
    }


}