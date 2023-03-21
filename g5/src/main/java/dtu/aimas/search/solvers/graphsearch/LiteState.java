package dtu.aimas.search.solvers.graphsearch;

import java.util.ArrayList;

import dtu.aimas.common.*;
import dtu.aimas.search.Action;

public class LiteState {
    public final LiteState parent;
    public ArrayList<Agent> agents;
    public ArrayList<Box> boxes;
    public Action[] jointActions;

    public LiteState(LiteState parent, ArrayList<Agent> agents, ArrayList<Box> boxes, Action[] jointActions){
        this.parent = parent;
        this.agents = agents;
        this.boxes = boxes;
        this.jointActions = jointActions;
    }
    
}
