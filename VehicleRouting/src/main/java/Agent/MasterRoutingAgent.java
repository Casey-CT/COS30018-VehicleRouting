package Agent;


import Agent.AgentInfo.AgentData;
import Communication.Message;
import DeliveryPath.Path;
import Item.Inventory;
import Item.Item;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;

public class MasterRoutingAgent extends Agent {
    private ArrayList<AgentData> agents = new ArrayList<>();

    public ArrayList<AgentData> getAgents() {
        return agents;
    }

    public void setAgents(ArrayList<AgentData> agents) {
        this.agents = agents;
    }

    public Inventory getMasterInventory() {
        return masterInventory;
    }

    public void setMasterInventory(Inventory masterInventory) {
        this.masterInventory = masterInventory;
    }

    public int[][] getMapData() {
        return mapData;
    }

    public void setMapData(int[][] mapData) {
        this.mapData = mapData;
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Path> paths) {
        this.paths = paths;
    }

    protected void setup() {
        System.out.println(getAID().getLocalName() + ": I have been created");

        //Add Dummy Item Data
        masterInventory.addItem(new Item(1, "Item1", 2, 12, 1));
        masterInventory.addItem(new Item(2, "Item2", 5, 50, 1));
        masterInventory.addItem(new Item(3, "Item3", 7, 12, 1));
        masterInventory.addItem(new Item(4, "Item4", 6, 11, 1));
        masterInventory.addItem(new Item(5, "Item5", 1, 2, 1));
        masterInventory.addItem(new Item(6, "Item6", 9, 11, 1));
        masterInventory.addItem(new Item(7, "Item7", 3, 16, 1));
        masterInventory.addItem(new Item(8, "Item8", 3, 12, 1));
        masterInventory.addItem(new Item(9, "Item9", 6, 20, 1));

        try{
            Thread.sleep(5000);
        }catch(Exception ex){System.out.println("Sleeping caused an error");}

        addBehaviour(new processRoutes());
    }

    private Inventory masterInventory = new Inventory();
    private int[][] mapData;
    private ArrayList<Path> paths = new ArrayList<>();

    private class ListenForMessages extends CyclicBehaviour {
        public void action() {

            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                System.out.println(myAgent.getLocalName() + ": Message Received");

                String messageContent = msg.getContent();

                if(msg.getPerformative() == ACLMessage.INFORM) {
                    String[] splitContent = messageContent.split(":", 2);

                    if(splitContent[0].equals(Message.ARRIVE)) {
                        boolean set = false;
                        for (AgentData agent: agents) {
                            if(agent.matchData(msg.getSender())) {
                                try{
                                    agent.setCurrentLocation(Integer.parseInt(splitContent[1]));
                                    System.out.println(myAgent.getLocalName() + ": " + agent.getName().getLocalName() + " has arrived at " + splitContent[1]);
                                    set = true;
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        if(!set) {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": Received Message From Unknown Delivery Agent.");
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    else if(splitContent[0].equals(Message.DELIVERED)) {
                        boolean set = false;
                        for (AgentData agent: agents) {
                            if(agent.matchData(msg.getSender())) {
                                try{
                                    set = agent.inventory.removeItem(Integer.parseInt(splitContent[1]));
                                    System.out.println(myAgent.getLocalName() + ": " + agent.getName().getLocalName() + " has delivered package " + splitContent[1]);
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        if(!set) {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": Received Message From Unknown Delivery Agent.");
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    else if(msg.getPerformative() == ACLMessage.FAILURE) {
                        try {
                            throw new Exception(myAgent.getLocalName() + ": " + msg.getSender().getLocalName() + " has run into an error.");
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class processRoutes extends Behaviour {
        //Number of expected replies
        private int expReplies = 0;

        //Number of received replies
        private int replyCount = 0;

        //Message Template for replies
        private MessageTemplate mt;

        //Current Step of This Behaviour
        private int step = 0;

        //Is Behaviour Done?
        private boolean done = false;

        public void action() {
            switch(step) {
                case 0:
                    System.out.println(getLocalName() + ": Finding Delivery Agents");

                    //Find all Delivery Agents with AMS
                    //Send them a request for their information
                    AMSAgentDescription[] a = null;

                    try {
                        SearchConstraints c = new SearchConstraints();
                        c.setMaxResults((long) -1);
                        a = AMSService.search(myAgent, new AMSAgentDescription(), c);
                    } catch (Exception ex) {
                        System.out.println(myAgent.getLocalName() + ": AMS ERROR while Finding Delivery Agents" + ex );
                        ex.printStackTrace();
                        System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                        done = true;
                    }

                    //TODO: Find a more reliable solution for this
                    //Find delivery agent based on agent type, rather than name
                    for(int i = 0; i < a.length; i++) {
                        if(a[i].getName().toString().contains("DeliveryAgent")) {
                            agents.add(new AgentData(a[i].getName()));
                        }
                    }

                    //Debug Stuff - Can be removed/disabled
                    System.out.println(getLocalName() + ": Found " + agents.size() + " Delivery Agents");
                    for(AgentData agent: agents) {
                        System.out.println(agent.getName());
                    }

                    if(agents.size() > 0) {
                        ACLMessage capacity_request = new ACLMessage(ACLMessage.REQUEST);
                        for(AgentData agent: agents) {
                            capacity_request.addReceiver(agent.getName());
                        }
                        capacity_request.setContent(Message.STATUS);
                        capacity_request.setConversationId("processRoute");
                        capacity_request.setReplyWith("Request" + System.currentTimeMillis());
                        myAgent.send(capacity_request);

                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(capacity_request.getReplyWith()));

                        System.out.println(getLocalName() + ": Capacity Request Send to All Delivery Agents");
                        expReplies = agents.size();

                        step = 1;
                    }
                    else {
                        System.out.println("No Agents Found");
                        done = true;
                    }

                    break;

                case 1:
                    System.out.println(getLocalName() + ": Handling Capacity Request Responses");
                    ACLMessage capacity_response = myAgent.receive(mt);
                    if(capacity_response != null) {

                        if(capacity_response.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(capacity_response.getSender())) {
                                    String[] splitContent = capacity_response.getContent().split(",", 2);
                                    agent.setCapacity(Integer.parseInt(splitContent[0]));
                                    agent.setCurrentLocation(Integer.parseInt(splitContent[1]));
                                    System.out.println(myAgent.getLocalName() + ": " + agent.getName().getLocalName() + " - Capacity " + agent.getCapacity() + " - CurrentLocation " + agent.getCurrentLocation());
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + capacity_response.getSender().toString() + " supplied an invalid capacity");
                            } catch (Exception ex){
                                ex.printStackTrace();
                                System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                done = true;
                            }
                        }

                        replyCount++;
                        System.out.println(getLocalName() + ": Received " + replyCount + " replies out of " + expReplies);
                        if(replyCount >= expReplies) {
                            replyCount = 0;
                            step = 2;
                        }
                    }
                    else {
                        block();
                        System.out.println(myAgent.getLocalName() + ": Blocking While Handling Capacity Request");
                    }
                    break;

                case 2:
                    System.out.println(getLocalName() + ": Allocating Inventories and Paths to Each Delivery Agent");

                    /*
                    //TODO: Finish Working CSP Solver
                    //Data to Give CSP Solver
                    //Number of Items
                    int N = masterInventory.getLength();

                    //Node ID of each Items destination
                    int[] dest = new int[masterInventory.getLength()];
                    int i = 0;
                    for(Item item: masterInventory.getItems()) {
                        dest[i] = item.getDestination();
                        i++;
                    }

                    //Each Items Weight Variable
                    int[] weight = new int[masterInventory.getLength()];
                    i = 0;
                    for(Item item: masterInventory.getItems()) {
                        weight[i] = item.getWeight();
                        i++;
                    }

                    //Int ID of each Delivery agent. Would correspond to agent's index in the agents ArrayList
                    //This might not work, but I think this should be fine
                    int[] da = new int[agents.size()];
                    for(i = 0; i < agents.size(); i++) {
                        da[i] = i;
                    }

                    //Carrying Capacity of each Delivery Agent
                    int[] da_weight = new int[agents.size()];
                    i = 0;
                    for(AgentData agent: agents) {
                        da_weight[i] = agent.getCapacity();
                        i++;
                    }

                    //Map of Shortest Distances Between Each Node
                    int[][] loc_dist = {};

                    /*
                    //Testing Output of Data
                    System.out.println();
                    System.out.println("Number of Packages: " + N);
                    System.out.print("Package Destinations: ");
                    for(i = 0; i < dest.length; i++){
                        System.out.print(dest[i] + " ");
                    }
                    System.out.println();
                    System.out.print("Package Weights: ");
                    for(i = 0; i < weight.length; i++) {
                        System.out.print(weight[i] + " ");
                    }
                    System.out.println();
                    System.out.print("DA IDs: ");
                    for(i = 0; i < da.length; i++) {
                        System.out.print(da[i] + " ");
                    }
                    System.out.println();
                    System.out.print("DA Capacities: ");
                    for(i = 0; i < da_weight.length; i++) {
                        System.out.print(da_weight[i] + " ");
                    }
                    System.out.println();

                    //CSP Model
                    Model model = new Model("Vehicle Routing Solver");

                    //CSP Variable Objects
                    //Packages - To Be Assigned a DA
                    IntVar[] packages = model.intVarArray("packages", N, da[0], da[da.length - 1], false);
                    IntVar[] tot_weights = model.intVarArray("Total Weights", da.length, 0, Integer.MAX_VALUE, true);

                    //Tot_Dist - Total Distance of All Paths

                    //CSP Constraints

                    //Total Weight of Packages Assigned to a Truck Must Not Outweigh it's Capacity
                    for(i = 0; i < da.length; i++) {

                        model.arithm(tot_weights[i].getValue(), "<=", da_weight[i]).post();
                    }

                    //CSP Solver
                    //TODO: Figure out how to get the solver to order the packages efficiently
                    Solver solver = model.getSolver();

                    while(solver.solve()) {
                        Solution solution = solver.findSolution();
                        System.out.println(solution.toString());
                    }
                    */

                    //TODO: Replace Dummy Data with CSP Solver
                    Inventory i1 = new Inventory();
                    i1.addItem(masterInventory.getItem(1));
                    i1.addItem(masterInventory.getItem(2));
                    i1.addItem(masterInventory.getItem(3));

                    Inventory i2 = new Inventory();
                    i2.addItem(masterInventory.getItem(4));
                    i2.addItem(masterInventory.getItem(5));
                    i2.addItem(masterInventory.getItem(6));

                    Inventory i3 = new Inventory();
                    i3.addItem(masterInventory.getItem(7));
                    i3.addItem(masterInventory.getItem(8));
                    i3.addItem(masterInventory.getItem(9));

                    Path p1 = new Path(new int[]{1,3,5,4,7}, new int[]{4,3,2,5,2});
                    Path p2 = new Path(new int[]{6,1,2,9}, new int[]{2,5,3,1});
                    Path p3 = new Path(new int[]{2,3,6}, new int[]{5,3,6});

                    //Add Inventories and Paths to the AgentData Objects
                    Path[] paths = {p1, p2, p3};
                    Inventory[] inventories = {i1, i2, i3};

                    //Enable for Testing if DA handles having more agents than it needs
                    boolean tooManyAgents = true;
                    if(tooManyAgents) {
                        agents.get(0).setJsonInventory(inventories[0].serialize());
                        agents.get(0).setJsonPath(paths[0].serialize());
                    }
                    else {
                        int i = 0;
                        for(AgentData agent: agents) {
                            agent.setJsonInventory(inventories[i].serialize());
                            agent.setJsonPath(paths[i].serialize());
                            i++;
                        }
                    }

                    System.out.println(getLocalName() + ": Inventories and Paths Created and Assigned");

                    //Set up Message
                    ACLMessage inventory_add = new ACLMessage(ACLMessage.INFORM);
                    inventory_add.setConversationId("processRoute");
                    inventory_add.setReplyWith(Message.INVENTORY + System.currentTimeMillis());
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(inventory_add.getReplyWith()));

                    //Send Inventory to Each Agent
                    for(AgentData agent: agents) {
                        if(!agent.getJsonInventory().isEmpty()) {
                            inventory_add.clearAllReceiver();
                            inventory_add.addReceiver(agent.getName());
                            inventory_add.setContent(Message.INVENTORY + ":" + agent.getJsonInventory());
                            myAgent.send(inventory_add);
                        }
                        else {
                            System.out.println(getLocalName() + ": " + agent.getName() + " has been given no items to deliver.");
                            expReplies--;
                        }
                    }

                    System.out.println(getLocalName() + ": Inventories Sent to Delivery Agents");

                    step = 3;

                    break;

                case 3:
                    System.out.println(getLocalName() + ": Handling DA Inventory Responses");
                    //Process Inventory Replies
                    ACLMessage inventory_response = myAgent.receive(mt);
                    if(inventory_response != null) {

                        if(inventory_response.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if (agent.matchData(inventory_response.getSender())) {
                                    if(inventory_response.getContent().equals(Message.INVENTORY_SUCCESS)) {
                                        //Set the local copy of the agents inventory
                                        agent.inventory.addInventory(Inventory.deserialize(agent.getJsonInventory()));
                                        for(Item item: agent.inventory.getItems()) {
                                            //Remove each item given to the agent from the master inventory
                                            if(!masterInventory.removeItem(item.getId())) {
                                                try{
                                                    throw new Exception(myAgent.getLocalName() + ": ERROR - " + inventory_response.getSender().toString() + " has been given an item not from the master inventory");
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                    System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                                    done = true;
                                                }
                                            }
                                        }
                                    }
                                    else if(inventory_response.getContent().equals(Message.INVENTORY_FAILURE)) {
                                        try {
                                            throw new Exception(myAgent.getLocalName() + ": ERROR - " + inventory_response.getSender().toString() + " could not load supplied inventory");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                            done = true;
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + inventory_response.getSender().toString() + " replied with incorrect performative");
                            } catch (Exception ex){
                                ex.printStackTrace();
                                System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                done = true;
                            }
                        }

                        replyCount++;
                        System.out.println(getLocalName() + ": Received " + replyCount + " Responses out of " + expReplies);

                        if(replyCount >= expReplies) {
                            replyCount = 0;
                            step = 4;
                        }
                    }
                    else {
                        block();
                        System.out.println(myAgent.getLocalName() + ": Blocking While Handling DA Inventory Response");
                    }
                    break;

                case 4:
                    System.out.println(getLocalName() + ": Sending Paths to Each Delivery Agent");
                    //Send Paths to all DAs
                    //Set up Message
                    ACLMessage path_add = new ACLMessage(ACLMessage.INFORM);
                    path_add.setConversationId("processRoute");
                    path_add.setReplyWith(Message.PATH + System.currentTimeMillis());
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(path_add.getReplyWith()));

                    //Send Inventory to Each Agent
                    for(AgentData agent: agents) {
                        if(!agent.getJsonPath().isEmpty()) {
                            path_add.clearAllReceiver();
                            path_add.addReceiver(agent.getName());
                            path_add.setContent(Message.PATH + ":" + agent.getJsonPath());
                            myAgent.send(path_add);
                        }
                    }

                    System.out.println(getLocalName() + ": Paths Sent to All Delivery Agents");

                    step = 5;
                    break;

                case 5:
                    System.out.println(getLocalName() + ": Handling DA Path Responses");
                    //Process all replies
                    ACLMessage path_response = myAgent.receive(mt);
                    if(path_response != null) {

                        if(path_response.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(path_response.getSender())) {
                                    //Set the path in AgentData Object here, but we don't need to do anything in here for now.
                                }
                            }
                            if(path_response.getContent().equals(Message.PATH_SUCCESS)) {
                                //Do Nothing, this is what we want
                            }
                            else if(path_response.getContent().equals(Message.PATH_FAILURE)) {
                                try {
                                    throw new Exception(myAgent.getLocalName() + ": ERROR - " + path_response.getSender().toString() + " could not load supplied path");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                    done = true;
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + path_response.getSender().toString() + " replied with incorrect performative");
                            } catch (Exception ex){
                                ex.printStackTrace();
                                System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                done = true;
                            }
                        }

                        replyCount++;
                        System.out.println(getLocalName() + ": Received " + replyCount + " Replies out of " + expReplies);

                        if(replyCount >= expReplies) {
                            replyCount = 0;
                            step = 6;
                        }
                    }
                    else {
                        block();
                        System.out.println(myAgent.getLocalName() + ": Blocking While Handling DA Path Response");
                    }
                    break;

                case 6:
                    System.out.println(getLocalName() + ": Sending All Delivery Agents Start Request");
                    //Tell all DAs to Start
                    ACLMessage start = new ACLMessage(ACLMessage.REQUEST);
                    for(AgentData agent: agents) {
                        //Only tell agent to start if agent has items to deliver
                        if(!agent.inventory.isEmpty()) {
                            start.addReceiver(agent.getName());
                        }
                    }
                    start.setContent(Message.START);
                    myAgent.send(start);

                    System.out.println(getLocalName() + ": Delivery Agents Requested to Start");

                    done = true;

                    //This Behaviour Has Finished, So start processing regular messages
                    //If this behaviour ever has to be rerun, remove the ListenForMessages behaviour
                    addBehaviour(new ListenForMessages());

                    break;

                default:
                    throw new RuntimeException(getLocalName() + ": beginRouting is at an invalid step");
            }
        }

        public boolean done() {
            return done;
        }
    }
}
