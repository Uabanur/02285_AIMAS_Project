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
import java.util.stream.Collectors;

import dtu.aimas.communication.IO;
import dtu.aimas.common.*;

import dtu.aimas.errors.InvalidOperation;
import dtu.aimas.errors.UnreachableState;
import dtu.aimas.search.Action;
import dtu.aimas.search.ActionType;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.SolutionMerger;
import dtu.aimas.search.solvers.conflictbasedsearch.Conflict;
import dtu.aimas.search.solvers.safeinterval.SafeProblem;
import dtu.aimas.search.solutions.StateSolution;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

public record StateSpace(
        @Getter @NonNull Problem problem,
        @Getter @NonNull State initialState
) {
    private static final Random RNG = new Random(1);

    public Result<Solution> createSolution(State state) {
        if (!isGoalState(state))
            return Result.error(new InvalidOperation("Can only create a solution from a goal state"));

        // return Result.ok(new ActionSolution(extractPlanFromState(state)));
        return Result.ok(new StateSolution(extractStates(state)));
    }

    public boolean isGoalState(State state) {
        for (Goal goal : this.problem.agentGoals) {
            // var agent = getAgentByNumber(state, goal.label - '0');
            var agent = getAgentByLabel(state, goal.label);
            if (!satisfies(goal, agent)) {
                return false;
            }
        }
        for (Goal goal : this.problem.boxGoals) {
            boolean goalSatisfied = false;
            for (Box box : state.boxes) {
                if (satisfies(goal, box)) {
                    goalSatisfied = true;
                    break;
                }
            }
            if (!goalSatisfied) {
                return false;
            }
        }

        return problem.validGoalState(state);
    }

    private State[] extractStates(State state) {
        var length = state.g() + 1;
        var states = new State[length];
        var current = state;

        for (var i = length - 1; i >= 0; i--) {
            states[i] = current;
            current = current.parent;
        }

        return states;
    }

    public Action[][] extractPlanFromState(State state)
    { 
        ArrayList<Action[]> plan = new ArrayList<Action[]>();
        State iterator = state;
        while (iterator.jointAction != null) {
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

                if (result.isError()) {
                    // A problem occurred with the result -> Silently return an empty list
                    return new Action[][] { };
                }

                Solution agentSolution = result.get();

                var agentSteps = new ArrayList<>(agentSolution.serializeSteps());

                if (stepIndex < agentSteps.size()) {
                    String agentStep = (String) agentSteps.get(stepIndex);
                    agentActions.add(Action.fromName(agentStep));
                    longestSolutionReached = false;
                } else {
                    // If an agent's solution was reached, but we are still investigating other agents, pad this agent's solution with NoOp
                    agentActions.add(Action.NoOp);
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

    public Agent getAgentByLabel(State state, char label) {
        for (var agent : state.agents) {
            if (agent.label == label) return agent;
        }
        throw new UnreachableState();
    }

    public Optional<Box> getBoxAt(State state, Position position) {
        for (Box box : state.boxes) {
            if (position.equals(box.pos)) {
                return Optional.of(box);
            }
        }
        return Optional.empty();
    }

    public Optional<Agent> getAgentAt(State state, Position position) {
        for (Agent agent : state.agents) {
            if (position.equals(agent.pos))
                return Optional.of(agent);
        }
        return Optional.empty();
    }

    private boolean satisfies(Goal goal, Agent agent) {
        return agent.pos.equals(goal.destination);
    }

    private boolean isCellFree(Position position, State state, Agent agent, int timeStep){
        return !getAgentAt(state, position).isPresent() && 
        !getBoxAt(state, position).isPresent() && this.problem.isFree(position, agent, timeStep);
    }

    private boolean canStayAtCell(Position position, Agent agent, int timeStep){
        return this.problem.isFree(position, agent, timeStep);
    }

    private boolean notOwner(Agent agent, Box box) {
        return agent.color != box.color;
    }

    private boolean satisfies(Goal goal, Box box) {
        return box.label == goal.label && box.pos.equals(goal.destination);
    }

    private Position moveAgent(Agent agent, Action action) {
        return new Position(agent.pos.row + action.agentRowDelta, agent.pos.col + action.agentColDelta);
    }

    private Position moveBox(Box box, Action action) {
        return new Position(box.pos.row + action.boxRowDelta, box.pos.col + action.boxColDelta);
    }

    private Position getPullSource(Agent agent, Action action) {
        return new Position(agent.pos.row - action.boxRowDelta, agent.pos.col - action.boxColDelta);
    }

    public boolean isApplicable(State state, Agent agent, Action action) {
        // Users of this method that don't care about timestamp redirect to the method with extended signature -- 
        // The argument of -1 ensures that time constraints do not restrict applicability: 
        // Reserving a cell at timestep of -1 should never happen
        return isApplicable(state, agent, action, -1);
    }

    public Optional<Position> findConflictingPosition(State state, Agent agent, Action action, int timeStep){
        Position agentDestination;
        Optional<Box> boxResult;
        Box box;
        switch (action.type) {
            case NoOp -> {
                return canStayAtCell(agent.pos, agent, timeStep)
                        ? Optional.empty()
                        : Optional.of(agent.pos);
            }
            case Move -> {
                agentDestination = moveAgent(agent, action);
                return isCellFree(agentDestination, state, agent, timeStep)
                        ? Optional.empty()
                        : Optional.of(agentDestination);
            }
            case Push -> {
                agentDestination = moveAgent(agent, action);
                boxResult = getBoxAt(state, agentDestination);
                if (boxResult.isEmpty()) return Optional.of(agentDestination);
                box = boxResult.get();
                if (notOwner(agent, box)) return Optional.of(agentDestination);
                Position boxDestination = moveBox(box, action);
                return isCellFree(boxDestination, state, agent, timeStep)
                        ? Optional.empty()
                        : Optional.of(boxDestination);
            }
            case Pull -> {
                Position boxSource = getPullSource(agent, action);
                boxResult = getBoxAt(state, boxSource);
                if (boxResult.isEmpty()) return Optional.of(boxSource);
                box = boxResult.get();
                if (notOwner(agent, box)) return Optional.of(boxSource);
                agentDestination = moveAgent(agent, action);
                return isCellFree(agentDestination, state, agent, timeStep)
                        ? Optional.empty()
                        : Optional.of(agentDestination);
            }
        }

        throw new UnreachableState();
    }

    public boolean isApplicable(State state, Agent agent, Action action, int timeStep){
        return findConflictingPosition(state,agent,action,timeStep).isEmpty();
//        Position agentDestination;
//        Optional<Box> boxResult;
//        Box box;
//        switch (action.type) {
//            case NoOp -> {
//                return canStayAtCell(agent.pos, agent, timeStep);
//            }
//            case Move -> {
//                agentDestination = moveAgent(agent, action);
//                return isCellFree(agentDestination, state, agent, timeStep);
//            }
//            case Push -> {
//                agentDestination = moveAgent(agent, action);
//                boxResult = getBoxAt(state, agentDestination);
//                if (boxResult.isEmpty()) return false;
//                box = boxResult.get();
//                if (notOwner(agent, box)) return false;
//                Position boxDestination = moveBox(box, action);
//                return isCellFree(boxDestination, state, agent, timeStep);
//            }
//            case Pull -> {
//                Position boxSource = getPullSource(agent, action);
//                boxResult = getBoxAt(state, boxSource);
//                if (boxResult.isEmpty()) return false;
//                box = boxResult.get();
//                if (notOwner(agent, box)) return false;
//                agentDestination = moveAgent(agent, action);
//                return isCellFree(agentDestination, state, agent, timeStep);
//            }
//        }
//
//        throw new UnreachableState();
    }

    public ArrayList<State> expand(State state) {

        int timeStep = 1;
        State iterator = state;
        while (iterator.parent != null) {
            iterator = iterator.parent;
            timeStep++;
        }

        int agentsCount = state.agents.size();
        Action[][] applicableActions = new Action[agentsCount][];
        for (int agentId = 0; agentId < agentsCount; agentId++) {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            // var agent = getAgentByNumber(state, agentId);
            var agent = state.agents.get(agentId);
            for(Action action : Action.values()){
                if(isApplicable(state, agent, action, timeStep)){
                    agentActions.add(action);
                }
            }
            // when no action is applicable due to constraints, return empty list
            if(agentActions.isEmpty()){
                return new ArrayList<>();
            }
            applicableActions[agentId] = agentActions.toArray(new Action[0]);
        }

        // permutations generation, literally copied from warmup
        Action[] jointAction = new Action[agentsCount];
        int[] actionsPermutation = new int[agentsCount];
        ArrayList<State> expandedStates = new ArrayList<>(16);

        while (true) {
            for (int i = 0; i < agentsCount; i++) {
                jointAction[i] = applicableActions[i][actionsPermutation[i]];
            }

            var stateResult = tryCreateState(state, jointAction);
            stateResult.ifPresent(expandedStates::add);

            boolean done = false;

            for (int i = 0; i < agentsCount; i++) {
                if (actionsPermutation[i] < applicableActions[i].length - 1) {
                    ++actionsPermutation[i];
                    break;
                } else {
                    actionsPermutation[i] = 0;
                    if (i == agentsCount - 1) {
                        done = true;
                    }
                }
            }

            if (done) break;
        }

//        Collections.shuffle(expandedStates, StateSpace.RNG);

        return expandedStates;
    }

    public boolean isValid(State state) {
        Set<Position> occupiedPositions = new HashSet<>();
        for (Agent agent : state.agents) {
            if (!occupiedPositions.add(agent.pos)) return false;
        }
        for (Box box : state.boxes) {
            if (!occupiedPositions.add(box.pos)) return false;
        }
        if (problem instanceof SafeProblem){
            for(var agent: state.agents){
                if(!problem.isFree(agent.pos, null, state.g())) return false;
            }
            for(var box: state.boxes){
                if(!problem.isFree(box.pos, null, state.g())) return false;
            }
        }
        return true;
    }

    public Optional<State> tryCreateState(State state, Action[] jointAction) {
        var jointActionsToApply = Arrays.copyOf(jointAction, jointAction.length);
        State destinationState = applyJointActions(state, jointActionsToApply);
        return isValid(destinationState) ? Optional.of(destinationState) : Optional.empty();
    }

    public Optional<Conflict> tryGetConflict(State nextState, int step){
        Optional<Conflict> conflict = Optional.empty();
        var state = nextState.parent;
        var actions = nextState.jointAction;
        
        // CASE 1: vertex conflict
        conflict = tryGetVertexConflict(nextState, step);
        
        // CASE 2: edge/follow conflict
        for(int agentNumber = 0; agentNumber < actions.length; agentNumber++){
            var performingAgent = getAgentByNumber(state, agentNumber);
            var action = actions[agentNumber];

            var possibleConflictPosition = getPossibleConflictPosition(state, performingAgent, action);
            
            // CASE: NOOP action
            if(!possibleConflictPosition.isPresent()) continue;

            // CASE: already investigating conflict on different position
            if(conflict.isPresent() && !conflict.get().getPosition().equals(possibleConflictPosition.get())) continue;

            var occupyingAgent = getAgentAt(state, possibleConflictPosition.get());
            var occupyingBox = getBoxAt(state, possibleConflictPosition.get());

            // CASE: cell not occupied
            if(!occupyingAgent.isPresent() && !occupyingBox.isPresent()) continue;

            // CASE: if no conflict detected yet, create new one
            if(!conflict.isPresent()) conflict = Optional.of(new Conflict(possibleConflictPosition.get(), step));
            
            conflict.get().involveAgent(getAgentFromInitialState(performingAgent));

            // CASE: agent is occupying the cell
            if(occupyingAgent.isPresent()) conflict.get().involveAgent(getAgentFromInitialState(occupyingAgent.get()));

            // CASE: box is occupying the cell
            if(occupyingBox.isPresent()) {
                var responsibleAgent = tryGetAgentResponsibleForBox(occupyingBox.get(), nextState);
                if(responsibleAgent.isPresent()) conflict.get().involveAgent(getAgentFromInitialState(responsibleAgent.get()));
            }
        }
        return conflict;
    }

    private Optional<Agent> tryGetAgentResponsibleForBox(Box box, State state){
        // APPROACH DESCRIPTION:
        // take the closest agent of the same color as responsible for the box
        Optional<Agent> responsibleAgent = Optional.empty();
        var closestDistance = Integer.MAX_VALUE;
        for(Agent agent : state.agents){
            if(!notOwner(agent, box)){
                var distance = Math.abs(agent.pos.row - box.pos.row) + Math.abs(agent.pos.col - box.pos.col);
                if(closestDistance > distance){
                    distance = closestDistance;
                    responsibleAgent = Optional.of(agent);
                }
            }
        }
        return responsibleAgent;
    }

    public Agent getAgentFromInitialState(Agent agentInCurrentState){
        return initialState.agents.stream().filter(agent -> agent.label == agentInCurrentState.label).findFirst().get();
    }

    public Optional<Conflict> tryGetVertexConflict(State state, int timeStep){
        Set<Position> occupiedPositions = new HashSet<>();
        Optional<Position> conflictPosition = Optional.empty();
        for (Agent agent : state.agents) {
            if (!occupiedPositions.add(agent.pos)){
                conflictPosition = Optional.of(agent.pos);
                break;
            }
        }
        if(!conflictPosition.isPresent()){
            for (Box box : state.boxes) {
                if (!occupiedPositions.add(box.pos)) {
                    if (!occupiedPositions.add(box.pos)){
                        conflictPosition = Optional.of(box.pos);
                        break;
                    }
                }
            }
        }
        if(!conflictPosition.isPresent()) return Optional.empty();
        var pos = conflictPosition.get();
        HashSet<Agent> involvedAgents = new HashSet<Agent>(state.agents.stream()
                                                                        .filter(agent -> agent.pos.equals(pos))
                                                                        .map(agent -> getAgentFromInitialState(agent))
                                                                        .collect(Collectors.toSet()));
        var involvedBoxesSet = state.boxes.stream().filter(box -> box.pos.equals(pos)).collect(Collectors.toSet());
        for(var box : involvedBoxesSet){
            var responsibleAgent = tryGetAgentResponsibleForBox(box, state);
            if(responsibleAgent.isPresent()) involvedAgents.add(getAgentFromInitialState(responsibleAgent.get()));
        }

        return Optional.of(new Conflict(conflictPosition.get(), timeStep, involvedAgents));
    }

    public Optional<Position> getPossibleConflictPosition(State state, Agent agent, Action action){
        switch (action.type) {
            case NoOp -> {
                return Optional.empty();
            }
            case Move -> {
                return Optional.of(moveAgent(agent, action));
            }
            case Push -> {
                var agentDestination = moveAgent(agent, action);
                var agentsBox = getBoxAt(state, agentDestination).get();
                return Optional.of(moveBox(agentsBox, action));
            }
            case Pull -> {
                return Optional.of(moveAgent(agent, action));
            }
            default -> throw new UnreachableState();
        }
    }

    public State applyJointActions(State state, Action[] actionsToApply) {
        ArrayList<Agent> updatedAgents = new ArrayList<>(state.agents.size());
        ArrayList<Box> updatedBoxes = new ArrayList<>(state.boxes.size());

        for (Box box : state.boxes) {
            updatedBoxes.add(copyBox(box));
        }

        for (int action = 0; action < actionsToApply.length; action++) {
            // Agent agent = getAgentByNumber(state, action);
            Agent agent = state.agents.get(action);
            Agent updatedAgent = null;
            Position agentDestination;

            Box box;
            Optional<Box> boxResult;
            Box updatedBox;
            Position boxDestination;
            Position boxSource;

            switch (actionsToApply[action].type) {
                case NoOp -> updatedAgent = copyAgent(agent);
                case Move -> {
                    agentDestination = moveAgent(agent, actionsToApply[action]);
                    updatedAgent = copyAgent(agent, agentDestination);
                }
                case Push -> {
                    agentDestination = moveAgent(agent, actionsToApply[action]);
                    boxResult = getBoxAt(state, agentDestination);
                    if(boxResult.isEmpty()) throw new IllegalStateException("Invalid action");
                    box = boxResult.get();
                    boxDestination = moveBox(box, actionsToApply[action]);
                    updatedAgent = copyAgent(agent, agentDestination);
                    updatedBox = copyBox(box, boxDestination);
                    for (int i = 0; i < updatedBoxes.size(); i++) {
                        if (agentDestination.equals(updatedBoxes.get(i).pos)) {
                            updatedBoxes.remove(i);
                            updatedBoxes.add(i, updatedBox);
                            break;
                        }
                    }
                }
                case Pull -> {
                    boxSource = getPullSource(agent, actionsToApply[action]);
                    agentDestination = moveAgent(agent, actionsToApply[action]);
                    boxResult = getBoxAt(state, boxSource);
                    if(boxResult.isEmpty()) throw new IllegalStateException("Invalid action");
                    box = boxResult.get();
                    boxDestination = moveBox(box, actionsToApply[action]);
                    updatedAgent = copyAgent(agent, agentDestination);
                    updatedBox = copyBox(box, boxDestination);
                    for (int i = 0; i < updatedBoxes.size(); i++) {
                        if (boxSource.equals(updatedBoxes.get(i).pos)) {
                            updatedBoxes.remove(i);
                            updatedBoxes.add(i, updatedBox);
                            break;
                        }
                    }
                }
            }
            updatedAgents.add(updatedAgent);
        }
        return new State(state, updatedAgents, updatedBoxes, actionsToApply);
    }

    private Agent copyAgent(Agent agent) {
        return new Agent(new Position(agent.pos.row, agent.pos.col), agent.color, agent.label);
    }

    private Agent copyAgent(Agent agent, Position newPosition) {
        return new Agent(newPosition, agent.color, agent.label);
    }

    private Box copyBox(Box box) {
        return new Box(new Position(box.pos.row, box.pos.col), box.color, box.label);
    }

    private Box copyBox(Box box, Position newPosition) {
        return new Box(newPosition, box.color, box.label);
    }

    public int getSatisfiedAgentGoalsCount(State state) {
        var result = 0;
        for (Goal goal : this.problem.agentGoals) {
            var agent = getAgentByLabel(state, goal.label);
            if (satisfies(goal, agent)) {
                result++;
            }
        }
        return result;
    }

    public int getSatisfiedBoxGoalsCount(State state) {
        var result = 0;
        for (Goal goal : this.problem.boxGoals) {
            for (Box box : state.boxes) {
                if (satisfies(goal, box)) {
                    result++;
                    break;
                }
            }
        }
        return result;
    }

    public State shallowMerge(State mainState, State otherState){
        var agents = new ArrayList<>(mainState.agents);
        var boxes = new ArrayList<>(mainState.boxes);

        agents.addAll(otherState.agents);
        boxes.addAll(otherState.boxes);

        return new State(agents, boxes);
    }

    public State shallowMerge(List<State> states){
        var agents = new ArrayList<Agent>();
        var boxes = new ArrayList<Box>();
        for(var state: states){
            agents.addAll(state.agents);
            boxes.addAll(state.boxes);
        }

        return new State(agents, boxes);
    }
}