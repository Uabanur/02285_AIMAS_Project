package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;
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

    public Action[][] extractPlan(LiteState state)
    { 
        ArrayList<Action[]> plan = new ArrayList<Action[]>();
        LiteState iterator = state;
        while (iterator.jointAction != null)
        {
            plan.add(iterator.jointAction);
            iterator = iterator.parent;
        }
        return plan.toArray(new Action[plan.size()][]);
    }

    public Result<Solution> createSolution(LiteState state){
        if (!isGoalState(state)) 
            return Result.error(new InvalidOperation("Can only create a solution from a goal state"));

        return Result.ok(new ActionSolution(extractPlan(state)));
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

    private boolean satisfies(Goal goal, Box box){
        return box.type == goal.label && box.pos == goal.destination;
    }

    private boolean satisfies(Goal goal, Agent agent){
        return agent.pos == goal.destination;
    }

    public boolean isGoalState(LiteState state) {
        // are agents on their goals?
        for(Goal goal : this.problem.agentGoals){
            var agent = getAgentByNumber(state, goal.label);
            if(!satisfies(goal, agent)){
                return false;
            }
        }
        // is each box goal satisfied?
        for(Goal goal : this.problem.boxGoals) {
            boolean goalSatisfied = false;
            for(Box box : state.boxes){
                if(!satisfies(goal, box)){
                    goalSatisfied = true;
                    break;
                }
            }
            if(!goalSatisfied) return false;
        }
        // everything is satisfied
        return true;
    }

    private Position moveAgent(Agent agent, Action action){
        return new Position(agent.pos.row + action.agentRowDelta, agent.pos.col + action.agentColDelta);
    }

    private Position moveBox(Box box, Action action){
        return new Position(box.pos.row + action.agentRowDelta, box.pos.col + action.agentColDelta);
    }

    private Position getPullSource(Agent agent, Action action){
        return new Position(agent.pos.row - action.boxRowDelta, agent.pos.col - action.boxColDelta);
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
                agentDestination = moveAgent(agent, action);
                return isCellFree(agentDestination, currentState);

            case Push:
                agentDestination = moveAgent(agent, action);
                boxResult = getBoxAt(currentState, agentDestination);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false;
                Position boxDestination = moveBox(box, action);
                return isCellFree(boxDestination, currentState);

            case Pull:
                Position boxSource = getPullSource(agent, action);
                boxResult = getBoxAt(currentState, boxSource);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false; 
                agentDestination = moveAgent(agent, action);
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
                expandedStates.add(stateResult.get());
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

    private Agent copyAgent(Agent agent){
        return new Agent(new Position(agent.pos.row, agent.pos.col), agent.color);
    }

    private Agent copyAgent(Agent agent, Position newPosition){
        return new Agent(newPosition, agent.color);
    }

    private Box copyBox(Box box){
        return new Box(new Position(box.pos.row, box.pos.col), box.color, box.type);
    }

    private Box copyBox(Box box, Position newPosition){
        return new Box(newPosition, box.color, box.type);
    }

    private LiteState applyJointActions(LiteState state, Action[] actionsToApply){
        ArrayList<Agent> updatedAgents = new ArrayList<>(state.agents.size());
        ArrayList<Box> updatedBoxes = new ArrayList<>(state.boxes.size());
        
        for(Box box : state.boxes){
            updatedBoxes.add(copyBox(box));
        }

        for(int action = 0; action < actionsToApply.length; action++){
            Agent agent = getAgentByNumber(state, action);
            Agent updatedAgent = null;
            Position agentDestination;

            Box box;
            Optional<Box> boxResult;
            Box updatedBox;
            Position boxDestination;
            Position boxSource;
            
            switch (actionsToApply[action].type)
            {
                case NoOp:
                    updatedAgent = copyAgent(agent);
                    break;
                
                case Move:
                    agentDestination = moveAgent(agent, actionsToApply[action]);

                    updatedAgent = copyAgent(agent, agentDestination);
                    break;

                case Push:
                    agentDestination = moveAgent(agent, actionsToApply[action]);
                    boxResult = getBoxAt(state, agentDestination);
                    box = boxResult.get();
                    boxDestination = moveBox(box, actionsToApply[action]);

                    updatedAgent = copyAgent(agent, agentDestination);
                    updatedBox = copyBox(box, boxDestination);

                    // update pushed box by its id

                    break;

                case Pull:
                    boxSource = getPullSource(agent, actionsToApply[action]);
                    agentDestination = moveAgent(agent, actionsToApply[action]);
                    boxResult = getBoxAt(state, boxSource);
                    box = boxResult.get();
                    boxDestination = moveBox(box, actionsToApply[action]);

                    updatedAgent = copyAgent(agent, agentDestination);
                    updatedBox = copyBox(box, boxDestination);

                    // update pulled box by its id

                    break;
            }
            updatedAgents.add(updatedAgent);
        }
        return new LiteState(state, updatedAgents, updatedBoxes, actionsToApply);
    }

    private Optional<LiteState> tryCreateState(LiteState state, Action[] jointAction){
        LiteState destinationState = applyJointActions(state, jointAction);
        return isValid(destinationState) ? Optional.of(destinationState) : Optional.empty();
    }


}