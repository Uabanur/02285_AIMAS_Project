package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;
import java.util.Arrays;
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
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.ActionSolution;
import dtu.aimas.search.solutions.Solution;
import lombok.Getter;

public class StateSpace {
    private static final Random RNG = new Random(1);

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

        return Result.ok(new ActionSolution(extractPlan(state)));
    }

    public boolean isGoalState(State state) {
        for(Goal goal : this.problem.agentGoals){
            var agent = getAgentByNumber(state, goal.label - '0');
            if(!satisfies(goal, agent)){
                return false;
            }
        }
        for(Goal goal : this.problem.boxGoals) {
            boolean goalSatisfied = false;
            for(Box box : state.boxes){
                if(satisfies(goal, box)){
                    goalSatisfied = true;
                    break;
                }
            }
            if(!goalSatisfied) 
            {
                return false;
            }
        }
        return true;
    }

    public Action[][] extractPlan(State state)
    { 
        ArrayList<Action[]> plan = new ArrayList<Action[]>();
        State iterator = state;
        while (iterator.jointAction != null)
        {
            plan.add(iterator.jointAction);
            iterator = iterator.parent;
        }
        Collections.reverse(plan);
        return plan.toArray(new Action[plan.size()][]);
    }

    public Agent getAgentByNumber(State state, int i) {
        return state.agents.get(i);
    }

    public Optional<Box> getBoxAt(State state, Position position) {
        for(Box box : state.boxes){
            if (position.equals(box.pos)){
                return Optional.of(box);
            }
        }
        return Optional.empty();
    }

    public Optional<Agent> getAgentAt(State state, Position position){
        for(Agent agent : state.agents){
            if (position.equals(agent.pos))
                return Optional.of(agent);
        }
        return Optional.empty();
    }

    private boolean satisfies(Goal goal, Agent agent){
        return agent.pos.equals(goal.destination);
    }

    private boolean isWallAt(Position position){
        return this.problem.walls[position.row][position.col];
    }

    private boolean isCellFree(Position position, State state){
        return !isWallAt(position) && !getAgentAt(state, position).isPresent() && !getBoxAt(state, position).isPresent();
    }

    private boolean canMoveBox(Agent agent, Box box){
        return agent.color == box.color;
    }

    private boolean satisfies(Goal goal, Box box){
        return box.type == goal.label && box.pos.equals(goal.destination);
    }

    private Position moveAgent(Agent agent, Action action){
        return new Position(agent.pos.row + action.agentRowDelta, agent.pos.col + action.agentColDelta);
    }

    private Position moveBox(Box box, Action action){
        return new Position(box.pos.row + action.boxRowDelta, box.pos.col + action.boxColDelta);
    }

    private Position getPullSource(Agent agent, Action action){
        return new Position(agent.pos.row - action.boxRowDelta, agent.pos.col - action.boxColDelta);
    }

    private boolean isApplicable(State state, Agent agent, Action action){
        Position agentDestination;
        Optional<Box> boxResult;
        Box box;
        switch(action.type){
            case NoOp:
                return true;

            case Move:
                agentDestination = moveAgent(agent, action);
                return isCellFree(agentDestination, state);

            case Push:
                agentDestination = moveAgent(agent, action);
                boxResult = getBoxAt(state, agentDestination);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false;
                Position boxDestination = moveBox(box, action);
                return isCellFree(boxDestination, state);

            case Pull:
                Position boxSource = getPullSource(agent, action);
                boxResult = getBoxAt(state, boxSource);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false; 
                agentDestination = moveAgent(agent, action);
                return isCellFree(agentDestination, state);

        }
        // unreachable
        return false;
    }

    public ArrayList<State> expand(State state) {
        int agentsCount = state.agents.size();
        Action[][] applicableActions = new Action[agentsCount][];
        for(int agentId = 0; agentId < agentsCount; agentId++)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            var agent = getAgentByNumber(state, agentId);
            for(Action action : Action.values()){
                if(isApplicable(state, agent, action)){
                    agentActions.add(action);
                }
            }
            applicableActions[agentId] = agentActions.toArray(new Action[0]);
        }

        // permutations generation, literally copied from warmup
        Action[] jointAction = new Action[agentsCount];
        int[] actionsPermutation = new int[agentsCount];
        ArrayList<State> expandedStates = new ArrayList<>(16);

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

    private boolean isValid(State state){
        Set<Position> occupiedPositions = new HashSet<>();
        for (Agent agent : state.agents){
            if(!occupiedPositions.add(agent.pos)) return false;
        }
        for (Box box : state.boxes){
            if(!occupiedPositions.add(box.pos)) return false;
        }
        return true;
    }

    private Optional<State> tryCreateState(State state, Action[] jointAction){
        var jointActionsToApply = Arrays.copyOf(jointAction, jointAction.length);
        State destinationState = applyJointActions(state, jointActionsToApply);
        return isValid(destinationState) ? Optional.of(destinationState) : Optional.empty();
    }

    private State applyJointActions(State state, Action[] actionsToApply){
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
                    for(int i = 0; i < updatedBoxes.size(); i++){
                        if(agentDestination.equals(updatedBoxes.get(i).pos)){
                            updatedBoxes.remove(i);
                            updatedBoxes.add(i, updatedBox);
                            break;
                        }
                    }

                    break;

                case Pull:
                    boxSource = getPullSource(agent, actionsToApply[action]);
                    agentDestination = moveAgent(agent, actionsToApply[action]);
                    boxResult = getBoxAt(state, boxSource);
                    box = boxResult.get();
                    boxDestination = moveBox(box, actionsToApply[action]);

                    updatedAgent = copyAgent(agent, agentDestination);
                    updatedBox = copyBox(box, boxDestination);
                    for(int i = 0; i < updatedBoxes.size(); i++){
                        if(boxSource.equals(updatedBoxes.get(i).pos)){
                            updatedBoxes.remove(i);
                            updatedBoxes.add(i, updatedBox);
                            break;
                        }
                    }

                    break;
            }
            updatedAgents.add(updatedAgent);
        }
        return new State(state, updatedAgents, updatedBoxes, actionsToApply);
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

    public int getSatisfiedAgentGoalsCount(State state){
        var result = 0;
        for(Goal goal : this.problem.agentGoals){
            var agent = getAgentByNumber(state, goal.label - '0');
            if(satisfies(goal, agent)){
                result++;
            }
        }
        return result;
    }

    public int getSatisfiedBoxGoalsCount(State state){
        var result = 0;
        for(Goal goal : this.problem.boxGoals) {
            for(Box box : state.boxes){
                if(satisfies(goal, box)){
                    result++;
                    break;
                }
            }
        }
        return result;
    }

}