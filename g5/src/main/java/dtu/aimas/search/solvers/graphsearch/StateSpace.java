package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.DomainObject;
import dtu.aimas.common.Goal;
import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.InvalidOperation;
import dtu.aimas.errors.UnreachableState;
import dtu.aimas.search.Action;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.ActionSolution;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.conflictbasedsearch.Conflict;
import dtu.aimas.search.solutions.StateSolution;
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

        // return Result.ok(new ActionSolution(extractPlanFromState(state)));
        return Result.ok(new StateSolution(extractStates(state)));
    }

    public boolean isGoalState(State state) {
        for(Goal goal : this.problem.agentGoals){
            // var agent = getAgentByNumber(state, goal.label - '0');
            var agent = getAgentByLabel(state, goal.label);
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

    private State[] extractStates(State state){
        var length = state.g()+1;
        var states = new State[length];
        var current = state;

        for(var i = length-1; i >= 0; i--){
            states[i] = current;
            current = current.parent;
        }
    
        return states;
    }

    public Action[][] extractPlanFromState(State state)
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

    public Action[][] extractPlanFromSubsolutions(List<Map.Entry<Agent, Result<Solution>>> solutions) {

        ArrayList<Action[]> plan = new ArrayList<Action[]>();

        var stepIndex = 0;
        boolean longestSolutionReached = false;

        while (!longestSolutionReached) {
            longestSolutionReached = true;
            ArrayList<Action> agentActions = new ArrayList<Action>();

            for (var solutionEntry: solutions) {
                Result<Solution> result = solutionEntry.getValue();
                Solution agentSolution = result.get();

                var agentSteps = new ArrayList<>(agentSolution.serializeSteps());

                if (stepIndex < agentSteps.size()) {
                    String agentStep = (String) agentSteps.get(stepIndex);
                    agentActions.add(Action.fromName(agentStep));
                    longestSolutionReached = false;
                } else {
                    // If an agent's solution was reached, but we are still investigating other agents, pad this agent's solution with NoOp
                    agentActions.add(Action.fromName("NoOp"));
                }
            }

            Action[] actionArray = new Action[agentActions.size()];
            agentActions.toArray(actionArray);

            plan.add(actionArray);

            stepIndex++;
        }

        return plan.toArray(new Action[plan.size()][]);
    }

    public Agent getAgentByNumber(State state, int i) {
        return state.agents.get(i);
    }

    public Agent getAgentByLabel(State state, char label){ 
        for(var agent: state.agents){
            if(agent.label == label) return agent;
        }
        throw new UnreachableState();
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

    private boolean isCellFree(Position position, State state, Agent agent, int timeStep){
        return !isWallAt(position) && !getAgentAt(state, position).isPresent() && 
        !getBoxAt(state, position).isPresent() && this.problem.isFree(position, agent, timeStep);
    }

    private boolean canMoveBox(Agent agent, Box box){
        return agent.color == box.color;
    }

    private boolean satisfies(Goal goal, Box box){
        return box.label == goal.label && box.pos.equals(goal.destination);
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

    public boolean isApplicable(State state, Agent agent, Action action) {
        // Users of this method that don't care about timestamp redirect to the method with extended signature -- 
        // The argument of -1 ensures that time constraints do not restrict applicability: 
        // Reserving a cell at timestep of -1 should never happen
        return isApplicable(state, agent, action, -1);
    }

    public boolean isApplicable(State state, Agent agent, Action action, int timeStep){
        Position agentDestination;
        Optional<Box> boxResult;
        Box box;
        switch(action.type){
            case NoOp:
                return true;

            case Move:
                agentDestination = moveAgent(agent, action);
                return isCellFree(agentDestination, state, agent, timeStep);

            case Push:
                agentDestination = moveAgent(agent, action);
                boxResult = getBoxAt(state, agentDestination);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false;
                Position boxDestination = moveBox(box, action);
                return isCellFree(boxDestination, state, agent, timeStep);

            case Pull:
                Position boxSource = getPullSource(agent, action);
                boxResult = getBoxAt(state, boxSource);
                if(!boxResult.isPresent()) return false;
                box = boxResult.get();
                if(!canMoveBox(agent, box)) return false; 
                agentDestination = moveAgent(agent, action);
                return isCellFree(agentDestination, state, agent, timeStep);

        }
        // unreachable
        return false;
    }

    public ArrayList<State> expand(State state) {

        int timeStep = 0;
        State iterator = state;
        while (iterator.parent != null) {
            iterator = iterator.parent;
            timeStep++;
        }

        int agentsCount = state.agents.size();
        Action[][] applicableActions = new Action[agentsCount][];
        for(int agentId = 0; agentId < agentsCount; agentId++)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            // var agent = getAgentByNumber(state, agentId);
            var agent = state.agents.get(agentId);
            for(Action action : Action.values()){
                if(isApplicable(state, agent, action, timeStep)){
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

    public boolean isValid(State state){
        Set<Position> occupiedPositions = new HashSet<>();
        for (Agent agent : state.agents){
            if(!occupiedPositions.add(agent.pos)) return false;
        }
        for (Box box : state.boxes){
            if(!occupiedPositions.add(box.pos)) return false;
        }
        return true;
    }

    public Optional<State> tryCreateState(State state, Action[] jointAction){
        var jointActionsToApply = Arrays.copyOf(jointAction, jointAction.length);
        State destinationState = applyJointActions(state, jointActionsToApply);
        return isValid(destinationState) ? Optional.of(destinationState) : Optional.empty();
    }

    public ArrayList<Conflict> replaySolutionsForConflicts(List<Map.Entry<Agent, Result<Solution>>> solutions) {

        ArrayList<Conflict> allConflicts = new ArrayList<Conflict>();
        
        var stepIndex = 0;

        State currentState = this.initialState;
        boolean longestSolutionReached = false;

        while (!longestSolutionReached) {            
            longestSolutionReached = true;
            ArrayList<Action> agentActions = new ArrayList<Action>();

            for (var solutionEntry: solutions) {
                Result<Solution> result = solutionEntry.getValue();
                Solution agentSolution = result.get();

                var agentSteps = new ArrayList<>(agentSolution.serializeSteps());

                if (stepIndex < agentSteps.size()) {
                    String agentStep = (String) agentSteps.get(stepIndex);
                    agentActions.add(Action.fromName(agentStep));
                    longestSolutionReached = false;
                } else {
                    // If an agent's solution was reached, but we are still investigating other agents, pad this agent's solution with NoOp
                    agentActions.add(Action.fromName("NoOp"));
                }
            }

            Action[] actionArray = new Action[agentActions.size()];
            agentActions.toArray(actionArray);
            

            var nextState = this.applyJointActions(currentState, actionArray);
            ArrayList<Conflict> currentStepConflicts = this.checkStateForConflicts(currentState, nextState, actionArray, stepIndex++);

            allConflicts.addAll(currentStepConflicts);
        }

        return allConflicts;
    }

    // TODO: deprecated - to be changed by external ConflictChecker
    private ArrayList<Conflict> checkStateForConflicts(State previousState, State state, Action[] previousActions, int timeStep) {
        ArrayList<Conflict> foundConflicts = new ArrayList<Conflict>();

        var agentAndBoxPositions = new HashMap<Position, ArrayList<DomainObject>>();
        for (var agent : state.agents) {
            if (agentAndBoxPositions.containsKey(agent.pos)) {
                agentAndBoxPositions.get(agent.pos).add(agent);
            } else {
                var agentsAtPos = new ArrayList<DomainObject>();
                agentsAtPos.add(agent);
                agentAndBoxPositions.put(agent.pos, agentsAtPos);
            }
        }

        for (var box : state.boxes) {
            if (agentAndBoxPositions.containsKey(box.pos)) {
                agentAndBoxPositions.get(box.pos).add(box);
            } else {
                var boxesAtPos = new ArrayList<DomainObject>();
                boxesAtPos.add(box);
                agentAndBoxPositions.put(box.pos, boxesAtPos);
            }
        }


        // Report the conflicts
        for (var entry : agentAndBoxPositions.entrySet()) {
            var position = entry.getKey();
            var objectsAtPos = entry.getValue();
            ArrayList<Agent> involvedAgents = new ArrayList<Agent>();

            if (objectsAtPos.size() <= 1) {
                continue;
            }

            // We found a conflict. For each conflicting object: if it is an agents, report the agent; if it is a box, report the agent that moved it
            for (var domainObject: objectsAtPos) {
                if (domainObject instanceof Agent) {
                    involvedAgents.add((Agent) domainObject);
                } else if (domainObject instanceof Box) {
                    // TODO: We currently don't have enough information to determine with certainty which agent moved the box.
                    // Additionally we don't have a strategy of generally dealing with box conflicts. 
                    // For example, what if no agent moved the box, which objects should be involved in the conflict?
                    continue;
                }
            } 

            Agent[] matchingInitialStateAgents = getInitialStateAgents(involvedAgents).toArray(new Agent[involvedAgents.size()]);
            Conflict newConflict = new Conflict(position, timeStep, matchingInitialStateAgents);
            foundConflicts.add(newConflict);
        }

        return foundConflicts;
    }

    private ArrayList<Agent> getInitialStateAgents(ArrayList<Agent> agentCopies) {

        ArrayList<Agent> matchingInitialAgents = new ArrayList<>();

        for (var initialAgent: initialState.agents) {
            for (var agentCopy: agentCopies) {
                if (agentCopy.label == initialAgent.label) {
                    matchingInitialAgents.add(initialAgent);
                }
            }  
        }

        return matchingInitialAgents;
    }

    private State applyJointActions(State state, Action[] actionsToApply){
        ArrayList<Agent> updatedAgents = new ArrayList<>(state.agents.size());
        ArrayList<Box> updatedBoxes = new ArrayList<>(state.boxes.size());

        for(Box box : state.boxes){
            updatedBoxes.add(copyBox(box));
        }

        for(int action = 0; action < actionsToApply.length; action++){
            // Agent agent = getAgentByNumber(state, action);
            Agent agent = state.agents.get(action);
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
        return new Agent(new Position(agent.pos.row, agent.pos.col), agent.color, agent.label);
    }

    private Agent copyAgent(Agent agent, Position newPosition){
        return new Agent(newPosition, agent.color, agent.label);
    }

    private Box copyBox(Box box){
        return new Box(new Position(box.pos.row, box.pos.col), box.color, box.label);
    }

    private Box copyBox(Box box, Position newPosition){
        return new Box(newPosition, box.color, box.label);
    }

    public int getSatisfiedAgentGoalsCount(State state){
        var result = 0;
        for(Goal goal : this.problem.agentGoals){
            var agent = getAgentByLabel(state, goal.label);
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