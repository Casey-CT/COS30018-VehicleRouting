package Agent;

import Agent.AgentInfo.AgentData;
import Communication.Message;
import DeliveryPath.Path;
import GA.Location;
import GraphGeneration.GraphGen;
import GUI.MyAgentInterface;
import Item.Inventory;
import Item.Item;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.jgap.*;
import org.jgap.impl.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.chocosolver.util.tools.StatisticUtils.sum;

public class MasterRoutingAgent extends Agent implements MyAgentInterface {

    //Collection of AgentData Objects, to keep track of the state of each DA this Agent is aware of
    private ArrayList<AgentData> agents = new ArrayList<>();

    //Initial storage place of packages to be delivered. Once a package is sent to a DA, it is removed from this list,
    //and added to the Individual Inventory objects contained in each AgentData object
    private Inventory masterInventory = new Inventory();

    //Initial storage place of paths to be sent to DAs. Paths will likely be created based on the CSP solver, so this list will likely not be used, and can be removed
    private ArrayList<Path> paths = new ArrayList<>();

    //Map Data
    //GraphGen Object, for generating the Map Data Arrays
    private GraphGen graph;

    //2D Array of Direct Connections between nodes.
    //A value of 0 means there is no direct connection.
    private int[][] mapData;

    //2D Array, which contains the distances of the shortest path between any two nodes
    //eg; mapDist[i][j] = 5, means the shortest path between nodes i and j is 5.
    private int[][] mapDist;

    //2D Array, which contains additional arrays, which contain the actual nodes required to travel between any 2 nodes.
    //Exclusive of first node, but inclusive of second node.
    //eg; mapPaths[i][j] = {k, l, j}.
    private int[][][] mapPaths;

    //Boolean flag, to make sure additional processRoutes behaviours aren't added
    private boolean processing = false;

    private int totalCapacityOfAllDAs;


    //Field Getters and Setters
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

    public int[][] getMapDist() {
        return mapDist;
    }

    public void setMapDist(int[][] mapDist) {
        this.mapDist = mapDist;
    }

    public int[][][] getMapPaths() {
        return mapPaths;
    }

    public void setMapPaths(int[][][] mapPaths) {
        this.mapPaths = mapPaths;
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Path> paths) {
        this.paths = paths;
    }

    protected void setup() {
        System.out.println(getAID().getLocalName() + ": I have been created");

        registerO2AInterface(MyAgentInterface.class, this);



        //TODO: Remove this Dummy Data
        //Dummy Item Data
//        masterInventory.addItem(new Item(1, "Item1", 2, 12, 1));
//        masterInventory.addItem(new Item(2, "Item2", 3, 50, 1));
//        masterInventory.addItem(new Item(3, "Item3", 2, 12, 1));
//        masterInventory.addItem(new Item(4, "Item4", 4, 11, 1));
//        masterInventory.addItem(new Item(5, "Item5", 1, 2, 1));
//        masterInventory.addItem(new Item(6, "Item6", 2, 11, 1));
//        masterInventory.addItem(new Item(7, "Item7", 1, 16, 1));
//        masterInventory.addItem(new Item(8, "Item8", 2, 12, 1));
//        masterInventory.addItem(new Item(9, "Item9", 4, 20, 1));
//        masterInventory.addItem(new Item(10, "Item10", 2, 10, 1));
//        masterInventory.addItem(new Item(11, "Item11", 3, 15, 1));
//        masterInventory.addItem(new Item(12, "Item12", 2, 17, 1));
//        masterInventory.addItem(new Item(13, "Item13", 4, 9, 1));
//        masterInventory.addItem(new Item(14, "Item14", 1, 1, 1));
//        masterInventory.addItem(new Item(15, "Item15", 2, 7, 1));
//        masterInventory.addItem(new Item(16, "Item16", 1, 3, 1));
//        masterInventory.addItem(new Item(17, "Item17", 2, 16, 1));
//        masterInventory.addItem(new Item(18, "Item18", 4, 5, 1));




        /*
        //TODO: Replace this with the GraphGen code
        //Dummy Map Data
        //MapData
        mapData = new int[][]{{0, 1, 0, 0, 3},
                              {1, 0, 4, 0, 0},
                              {0, 4, 0, 1, 0},
                              {0, 0, 1, 0, 2},
                              {3, 0, 0, 2, 0}};

        //MapDist
        mapDist = new int[][]{{0, 17, 31, 12, 52},
                              {17, 0 ,12, 16, 91},
                              {31, 12, 0, 18, 32},
                              {12, 16, 18, 0, 26},
                              {52, 91, 32, 26, 0}};

        //MapPath
        mapPaths = new int[][][]{{{}, {1}, {1, 2}, {4, 3}, {4}},
                                 {{0}, {}, {2}, {2, 3}, {0, 4}},
                                 {{1, 0}, {1}, {}, {3}, {3, 4}},
                                 {{4, 0}, {2, 1}, {2}, {}, {4}},
                                 {{0}, {0, 1}, {3, 2}, {3}, {}}};
        */
        //TODO: Remove
        //Commenting out the adding of this behaviour, as it is now added by the GUI
        //addBehaviour(new processRoutes());
    }

    //TODO: Comment each individual message interpretation
    //Behaviour that constantly loops, listening for messages
    //Works in the same way as the ListenForMessages behaviour in the DeliveryAgent class
    //
    //This behaviour doesn't do anything while the processing boolean flag is set to true
    private class listenForMessages extends CyclicBehaviour {
        public void action() {
            if(!processing) {
                ACLMessage msg = myAgent.receive();

                if (msg != null) {
                    System.out.println(myAgent.getLocalName() + ": Message Received");

                    String messageContent = msg.getContent();

                    if(msg.getPerformative() == ACLMessage.INFORM) {

                        if(messageContent.contains(Message.ARRIVE)) {
                            String[] splitContent = messageContent.split(":", 2);
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
                                    throw new Exception(myAgent.getLocalName() + ": Received Arrival Message From Unknown Delivery Agent.");
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else if(messageContent.contains(Message.DELIVERED)) {
                            String[] splitContent = messageContent.split(":", 2);
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
                                    throw new Exception(myAgent.getLocalName() + ": Received Package Delivered Message From Unknown Delivery Agent.");
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else if(messageContent.contains(Message.COMPLETE)) {
                            boolean set = false;
                            for (AgentData agent: agents) {
                                if(agent.matchData(msg.getSender())) {
                                    try {
                                        set = true;

                                        if(agent.getCurrentLocation() != 0) {
                                            int[] pathLoc = mapPaths[agent.getCurrentLocation()][0];
                                            int[] pathDist = new int[pathLoc.length];

                                            pathDist[0] = mapDist[agent.getCurrentLocation()][pathLoc[0]];
                                            for(int i = 1; i < pathLoc.length; i++) {
                                                pathDist[i] = mapDist[pathLoc[i - 1]][pathLoc[i]];
                                            }

                                            String jsonPath = new Path(pathLoc, pathDist).serialize();

                                            ACLMessage reply = msg.createReply();
                                            reply.setPerformative(ACLMessage.REQUEST);
                                            reply.setContent(Message.RETURN + ":" + jsonPath);
                                            myAgent.send(reply);
                                        }
                                    } catch(Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                            if(!set) {
                                try {
                                    throw new Exception(myAgent.getLocalName() + ": Received Path Complete Message From Unknown Delivery Agent.");
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
    }

    //Behaviour that constantly loops its action() method, until the done() method returns true
    //What is run in the action() method, depends on the value of the step variable
    //
    //The Steps
    //Step 0: Find Agents, and request their status
    //  -Make sure the graph object is not null, otherwise end the behaviour
    //  -Set the processing flag to true
    //  -Using the AMSService, an AID list of all Agents on the System is returned
    //  -The list of Agents is looped through
    //  -If any Agent names contain "DeliveryAgent" and does not match an existing AgentData object, a new AgentData object is created using the AID of the agent
    //  -If no agents have been found, the done variable is set to true, and this behaviour terminates
    //  -Otherwise, an ACLMessage is created with its content requesting status
    //  -Each agent found is added as a receiver, then the message is sent
    //  -The expReplies variable is set to the number of Delivery Agents found
    //  -The step variable is then set to 1
    //
    //Step 1: Process Agent Replies
    //  -Checks for incoming messages
    //  -If no message has been received, this behaviour blocks until a message is received
    //  -If a message has been received, it is processed
    //      -The agents ArrayList is looped through, and the sending DA's AID is matched to the AID in the AgentData object
    //      -Using the string split method, the content is divided, and used to update the capacity and currentLocation variables in the matching AgentData object
    //      -The replyCount variable is then incremented
    //  -If any invalid messages are received, this behaviour throws an exception and terminates
    //  -Once replyCount matches expReplies
    //      -replyCount is reset to 0
    //      -The step variable is set to 2
    //
    //Step 2: Solve the CSP Problem, create and assign serialized inventories and paths, then send serialized inventories to Delivery Agents
    //  -Checks the total capacity of all DAs is greater or equal to total weight of Items in masterInventory
    //      -If less, terminates this behaviour
    //  -Runs the solveConstraintProblem() method
    //  -An ACLMessage object is created
    //  -Loops through the agents ArrayList
    //      -For each AgentData
    //      -If the jsonInventory variable is not empty:
    //          -Add the jsonInventory as message content
    //          -The AID of the AgentData is added as the receiver
    //          -The message is sent
    //      -If the jsonInventory variable is empty:
    //          -The expReplies variable is decremented
    //  -The step variable is set to 3
    //
    //Step 3: Process Agent Replies
    //  -Works the same as step 1
    //  -When processing a reply
    //      -If a success message is received
    //      -The json representation of the inventory stored in the AgentData object is serialized into an actual Inventory object and added to the AgentData
    //      -Each item in the AgentData's inventory is then removed from the masterInventory
    //      -The json representation saved in the AgentData object is cleared
    //
    //Step 4: Send serialized paths to Delivery Agents
    //  -Creates a new ACLMessage
    //  -Loops through the AgentData ArrayList
    //      -For each AgentData:
    //      -If the AgentData jsonPath field is not empty the jsonPath of the AgentData is set as the message content
    //      -The AID of the AgentData is added as the receiver
    //      -The message is sent
    //  -The step variable is set to 5
    //
    //Step 5: Process Agent Replies
    //  -Works the same as step 1
    //
    //Step 6: Send Delivery Agents a START message
    //  -Creates a new ACLMessage
    //  -Loops through the AgentData objects
    //  -If the inventory inside the AgentData record is not empty, the AID in the AgentData is added as a receiver
    //  -The message content is set to a start message and sent
    //  -The finishBehaviour() method is called
    private class processRoutes extends Behaviour {
        //int number of replies this agent is expecting. If agents are not given any items to deliver, this number will be decremented
        private int expReplies = 0;

        //int, to be used to keep track of the number of replies this agent has processed each step
        private int replyCount = 0;

        //Message Template for replies
        private MessageTemplate mt;

        //Current step of this behaviour
        private int step = 0;

        //When set to true, this behaviour terminates.
        private boolean done = false;

        public void action() {
            switch(step) {
                case 0:
                    System.out.println(getLocalName() + ": Finding Delivery Agents");

                    //This behaviour has begun, so set the processing flag
                    processing = true;

                    //Make Sure Map Data has been assigned
                    if(graph == null) {
                        System.out.println(getLocalName() + ": Map Data Has Not Been Assigned Yet. Stopping Behaviour");
                        finishBehaviour();
                        break;
                    }

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
                        finishBehaviour();
                    }

                    //TODO: Find a more reliable solution for this
                    //Find delivery agent based on agent type, rather than name
                    for(int i = 0; i < a.length; i++) {
                        if(a[i].getName().toString().contains("DeliveryAgent")) {
                            boolean newAgent = true;
                            for(AgentData agent: agents) {
                                if(agent.matchData(a[i].getName())) {
                                    newAgent = false;
                                }
                            }
                            if(newAgent) {
                                agents.add(new AgentData(a[i].getName()));
                                System.out.println(myAgent.getLocalName() + ": Created AgentData for " + a[i].getName());
                            }
                            else {
                                System.out.println(myAgent.getLocalName() + ": AgentData already exists for " + a[i].getName());
                            }
                        }
                    }

                    //Debug Stuff - Prints the AID of each Agent Found
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
                        //setConversationID() and setReplyWith() are used to make sure we only process replies to this message in the next step
                        capacity_request.setConversationId("processRoute");
                        capacity_request.setReplyWith("Request" + System.currentTimeMillis());
                        myAgent.send(capacity_request);

                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(capacity_request.getReplyWith()));

                        System.out.println(getLocalName() + ": Capacity Request Send to All Delivery Agents");
                        expReplies = agents.size();

                        step = 1;
                    }
                    else {
                        System.out.println(getLocalName() + ": No Agents Found");
                        finishBehaviour();
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
                                finishBehaviour();
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
                    //Total Weight of all Items in masterInventory
                    int weightTotal = masterInventory.getTotalWeight();

                    //Total Capacity of all Delivery Agents
                    int capacityTotal = 0;
                    for(AgentData agent: agents) {
                        capacityTotal += agent.getCapacity();
                    }

                    if(weightTotal > capacityTotal) {
                        System.out.println(myAgent.getLocalName() + ": Mismatch in Total DA Capacity and Total Inventory Weight. Stopping this Behaviour");
                        finishBehaviour();
                    }

                    System.out.println(getLocalName() + ": Allocating Inventories and Paths to Each Delivery Agent");

                    //Solve the constraint problem, and terminate if no solution is found
                    if(!solveConstraintProblem()) {
                        System.out.println(getLocalName() + ": No Solution Found. Stopping this Behaviour");
                        finishBehaviour();
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
                                        //Set the local copy of the agents inventory and clear the jsonRepresentation
                                        agent.inventory.addInventory(Inventory.deserialize(agent.getJsonInventory()));
                                        agent.clearJsonInventory();
                                        for(Item item: agent.inventory.getItems()) {
                                            //Remove each item given to the agent from the master inventory
                                            if(!masterInventory.removeItem(item.getId())) {
                                                try {
                                                    throw new Exception(myAgent.getLocalName() + ": ERROR - " + inventory_response.getSender().toString() + " has been given an item not from the master inventory");
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                    System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                                    System.out.println("ITEM ID THAT CAUSED ERROR: " +item.getId());
                                                    finishBehaviour();
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
                                            finishBehaviour();
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
                                finishBehaviour();
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
                                    //I don't think we need to keep track of the Delivery Agents Path, so this is ultimately unnecessary
                                    if(path_response.getContent().equals(Message.PATH_SUCCESS)) {
                                        //Clear the json Representation
                                        agent.clearJsonPath();
                                    }
                                    else if(path_response.getContent().equals(Message.PATH_FAILURE)) {
                                        try {
                                            throw new Exception(myAgent.getLocalName() + ": ERROR - " + path_response.getSender().toString() + " could not load supplied path");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                            finishBehaviour();
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + path_response.getSender().toString() + " replied with incorrect performative");
                            } catch (Exception ex){
                                ex.printStackTrace();
                                System.out.println(myAgent.getLocalName() + ": An Error Has Occurred. Stopping this behaviour");
                                finishBehaviour();
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
                        //If this behaviour ever needs to be rerun, this for loop will need to be altered
                        if(!agent.inventory.isEmpty()) {
                            start.addReceiver(agent.getName());
                        }
                    }
                    start.setContent(Message.START);
                    myAgent.send(start);

                    System.out.println(getLocalName() + ": Delivery Agents Requested to Start");

                    finishBehaviour();

                    break;

                default:
                    throw new RuntimeException(getLocalName() + ": processRoutes is at an invalid step");
            }
        }

        public boolean done() {
            return done;
        }

        public void finishBehaviour() {
            done = true;
            processing = false;
        }
    }

    //Function for solving the CSP problem, and processing the solution
    //This function is complicated, so it is commented throughout
    //TODO: Add a proper writeup of this function, once the code has been cleaned up properly
    //TODO: CleanUp/Remove the CSP code
    //TODO: Add return false if no solution can be found
    //Assigns json representation of paths and inventories to AgentData objects in the agents ArrayList
    //Returns if a solution is found and processed, false otherwise
    public boolean solveConstraintProblem() {
        //TODO: Remove unnecessary data
        //Data to Give CSP Solver

        //Number of Items
        int P = masterInventory.getLength();

        //TODO: Find a better solution that moving ints into an arraylist and then streaming
        //Node ID of each Items destination
        //Not in Use at the moment, but could be useful later on.
        int[] dest;

        ArrayList<Integer> temp = new ArrayList<>();
        for(Item item: masterInventory.getItems()) {
            temp.add(item.getDestination());
        }
        dest = temp.stream().mapToInt(o -> o).toArray();
        temp.clear();

        //Each Items Weight Variable
        int[] weight;
        for(Item item: masterInventory.getItems()) {
            temp.add(item.getWeight());
        }
        weight = temp.stream().mapToInt(o -> o).toArray();
        temp.clear();

        //Distance from each Item's Location to Node 0
        int[] roughDistances;
        for(Item item: masterInventory.getItems()) {
            temp.add(mapDist[0][item.getDestination() - 1]);
        }
        roughDistances = temp.stream().mapToInt(o -> o).toArray();
        temp.clear();

        //Number of Delivery Agents
        int D = agents.size();

        //Carrying Capacity of each Delivery Agent
        int[] da_capacity;
        for(AgentData agent: agents) {
            temp.add(agent.getCapacity());
        }
        da_capacity = temp.stream().mapToInt(o -> o).toArray();
        temp.clear();

        //The average weight per delivery agent is the sum of the total weights of all the packages divided by the number of delivery agents
        int averageWeightPerDA = sum(weight) / D;

        //GENETIC ALGORITHM:
        //List of all the delivery locations
        ArrayList<Location> locations = new ArrayList<>();

        //Index of locations
        //Each location has an index that will be used for calculating distance.
        int index = 1;
        //Creating a null location for use in the following for loop
        Location location = null;

        //Iterating over the rows
        for(int x = 0; x < mapDist.length; x++) {
            //Iterating over columns
            for(int y = 0; y < mapDist.length; y++) {
                //Create a new location at (x,y) having the given index, demand of 0
                location = new Location(x, y, index, 0);
                int demand = 0;
                //Check if any item is destined for that location
                for(int z = 1; z <= masterInventory.getLength(); z++) {
                    Item item = masterInventory.getItem(z);
                    if(item.getDestination() == index) {
                        //Add the weight of the item to the demand of that location
                        demand += item.getWeight();
                    }
                }
                //Set the overall demand for that location
                location.setDemand(demand);
                System.out.println("Location : " + index + " has a demand of: " + demand);
            }
            index++;
            location.setDistancesMatrix(mapDist);
            locations.add(location);
        }

        for(Location l: locations)  {
            System.out.println("THERE EXISTS A LOCATION WITH INDEX: " + l.getIndex());
        }

        //Genetic algorithm values
        int numberOfEvolutions = 2000;
        int populationSize = 100;

        int numberOfLocations = locations.size();

        //Fitness penalty when the total weight of packages assigned to a truck exceeds its capacity
        int truckOverloadPenalty = 500;
        //Fitness penalty when a truck's capacity is not completely utilized
        int incompleteTruckPenalty = 2;
        //Fitness penalty for the distance travelled by the truck
        int distancePenalty = 25;

        //Creating a fitnessfunction object with the above values
        GA.FitnessFunction fitnessFunction = new GA.FitnessFunction(D, numberOfLocations, da_capacity[0], truckOverloadPenalty, incompleteTruckPenalty, distancePenalty);
        fitnessFunction.setLocations(locations);

        //Creating a default JGAP configuration object
        Configuration configuration = new DefaultConfiguration();
        configuration.setPreservFittestIndividual(true);
        configuration.setKeepPopulationSizeConstant(true);

        //Creating two genetic operators to control mutation and crossover
        MutationOperator mutationOperator = null;
        CrossoverOperator crossoverOperator = null;

        //JGAP throws InvalidConfigurationException for these statments
        try {
            //Specifying the configuration to use our custom fitness function
            configuration.setFitnessFunction(fitnessFunction);
            configuration.setPopulationSize(populationSize);

            mutationOperator = new MutationOperator(configuration);
            //1 in 10 chromosomes will undergo mutation
            mutationOperator.setMutationRate(10);
            //Crossover rate is default
            crossoverOperator = new CrossoverOperator(configuration);
            crossoverOperator.setAllowFullCrossOver(true);

            configuration.addGeneticOperator(mutationOperator);
            configuration.addGeneticOperator(crossoverOperator);
        } catch (InvalidConfigurationException e) {
            System.out.println("Error in Genetic Algorithm configuration: " + e.getMessage());
        }

        //Initializing the genes array
        final Gene[] genes = new Gene[2 * numberOfLocations];

        for (int i = 0; i < numberOfLocations; i++) {
            try {
                genes[i] = new IntegerGene(configuration, 1, D);
                genes[i + numberOfLocations] = new DoubleGene(configuration, 0, numberOfLocations);
            } catch (InvalidConfigurationException e) {
                System.out.println("Error in Genetic Algorithm gene configuration: " + e.getMessage());
            }
        }
        //Store the population
        Genotype population = null;

        try {
            configuration.setSampleChromosome(new Chromosome(configuration, genes));
            //Generate a random initial population
            population = Genotype.randomInitialGenotype(configuration);
        } catch(InvalidConfigurationException e) {
            System.out.println("Invalid configuration" + e.getMessage());
        }

        System.out.println("Number of generations to evolve: " + numberOfEvolutions);

        for(int i = 1; i <= numberOfEvolutions; i++) {
            //Output the population details after every 100 evolutions
            if (i % 100 == 0) {
                IChromosome bestChromosome = population.getFittestChromosome();
                System.out.println("Best fitness after " + i + " generations: " + bestChromosome.getFitnessValue());
                double totalDistanceOfAllVehicles = 0.0;
                for(int j = 1; j <= D; j++) {
                    double routeDistance = fitnessFunction.computeTotalDistance(j, bestChromosome, fitnessFunction);
                    totalDistanceOfAllVehicles += routeDistance;
                }
                System.out.println("Total distance of all vehicles: " + totalDistanceOfAllVehicles);
            }
            //Population must evolve
            population.evolve();
        }

        //Store the overall fittest chromosome
        IChromosome bestChromosome = population.getFittestChromosome();
        double totalDistanceOfAllVehicles = 0.0;

        int flag = 0;

        ArrayList<List<Integer>> results = new ArrayList<>();

        //Store and print the optimum routes for the delivery agents
        for(int i = 1; i <= D; i++) {
            List<Integer> route = fitnessFunction.getPositions(i, bestChromosome, fitnessFunction, true);
            double routeDistance = fitnessFunction.computeTotalDistance(i, bestChromosome, fitnessFunction);
            double vehicleWeight = fitnessFunction.computeUsedCapacity(i, bestChromosome, fitnessFunction);

            List<Integer> result = new ArrayList<>(Collections.singletonList(1));
            result.addAll(route.stream().map(aList -> aList + 1).collect(Collectors.toList()));

            if(result.contains(new Integer(1))) {
                flag++;
            }

            if(flag > 1) {
                if (result.contains(new Integer(1))) {
                    result.remove(new Integer(1));
                }
            }

            results.add(result);
            System.out.println("Delivery vehicle " + i + " : " + result);
            System.out.println("Total distance " + routeDistance);
            System.out.println("Weight of packages delivered: " + vehicleWeight);
            totalDistanceOfAllVehicles += routeDistance;
        }

        System.out.println("Total distance of all vehicles: " + totalDistanceOfAllVehicles);

        //TODO: Make sure to avoid any out of bounds exceptions here
        //As a note, this assumes the order of results are the same as the order of the AgentData objects in agents
        for(int i = 0; i < agents.size(); i++) {

            //Assemble the Inventory
            //Creates a temp inventory
            Inventory tempInv = new Inventory();

            //Loops through each location in results[i]
            for(Integer in: results.get(i)) {
                //Loops through every item in the master inventory
                //If its node id of the item, matches then node id from results,
                //the item is added to the temp inventory
                for(Item item: masterInventory.getItems()) {
                    if(item.getDestination() == in) {
                        tempInv.addItem(item);
                        System.out.println("ITEM : " + item.getId() + " ASSIGNED TO DA " + i);
                    }
                }
            }

            //Serialize the assembled inventory, and add to the AgentData
            agents.get(i).setJsonInventory(tempInv.serialize());

            //TODO: This could be made more efficient
            // I've done it in this way as it lets me re-use some of the choco code
            //Assemble the Path
            //Iterates through the ordered items in tempInv
            //For each node between the item and previous item
            //The node ids are added to the loc ArrayList
            //The distances are added to the dist ArrayList
            ArrayList<Integer> loc = new ArrayList<>();
            ArrayList<Integer> dist = new ArrayList<>();
            int prev_loc = agents.get(i).getCurrentLocation();
            for(Item item: tempInv.getItems()) {
                if(item.getDestination() != prev_loc) {
                    int[] next_dest = mapPaths[prev_loc][item.getDestination() - 1];
                    for(int o = 0; o < next_dest.length; o++) {
                        loc.add(next_dest[o]);
                        dist.add(mapData[prev_loc][o]);
                        prev_loc = next_dest[o];
                    }
                }
            }

            //The loc and dist ArrayLists are converted to arrays
            //These arrays are used to create a Path object
            //This path is serialized and added to the AgentData object
            int[] loc_array = loc.stream().mapToInt(o -> o).toArray();
            int[] dist_array = dist.stream().mapToInt(o -> o).toArray();
            Path path = new Path(loc_array, dist_array);
            agents.get(i).setJsonPath(path.serialize());
        }


        /*
        //OLD CHOCO CODE

        //The Model
        Model model = new Model("Vehicle Routing Solver");

        //Variables
        //Boolean Variable for Each Combination of Package and DA
        //If a variable is true, it means that DA is delivery that package
        //eg; if Packages[i][j] is true, then DA j is delivering Package i
        BoolVar[][] Packages = new BoolVar[P][D];
        for(int i = 0; i < P; i++) {
            for(int j = 0; j < D; j++) {
                Packages[i][j] = model.boolVar("Package " + i + " - DA " + j + ": ");
            }
        }

        //Int Variable for the total weight of packages assigned to a particular DA.
        //eg; Tot_Weights[i] is the total weight of packages assigned to DA i
        //The Value of these variables is calculated with a SCALAR constraint
        IntVar[] Tot_Weights = new IntVar[D];
        for(int i = 0; i < D; i++) {
            Tot_Weights[i] = model.intVar("DA " + i + "Capacity", 0, da_capacity[i]);
        }

        //Int Variable for the total "rough" distance of each path
        //eg; Tot_RoughPath[i] is the total rough path distance of packages assigned to DA i
        //The Value of these variables is calculated with a SCALAR constraint
        IntVar[] Tot_RoughPath = new IntVar[D];
        for(int i = 0; i < D; i++) {
            Tot_RoughPath[i] = model.intVar("DA " + i + " RoughPath", 0, IntVar.MAX_INT_BOUND);
        }

        //Int Variable for the total number of packages assigned to each DA
        //eg; Tot_Packages[i] is the total number of packages assigned to DA i
        //The Value of these variables is calculated with a SCALAR constraint, using Packages_Coeff as its coefficients
        IntVar[] Tot_Packages = new IntVar[D];
        for(int i = 0; i < D; i++) {
            Tot_Packages[i] = model.intVar("DA " + i + " Package Total", 0, IntVar.MAX_INT_BOUND);
        }

        //Single IntVar to be used as a total of all rough paths
        //This variable will be used as the "objective" in the code
        //The value will be calculated in a SCALAR constraint, using Path_Total_Coeff as its coefficients
        IntVar Path_Total = model.intVar("Total Path Length", 0, IntVar.MAX_INT_BOUND);

        //TODO: Find a better method of totalling variables
        //Scalar Coefficient Arrays
        //As a SCALAR constraint requires the number of coefficients and variables to be the same,
        //to use a SCALAR constraint to sum variables (which I'm not sure is even a good idea, but it works),
        //all the coefficients need to be 1.

        //Array of length P (number of packages)
        int[] Packages_Coeff = new int [P];
        for(int i = 0; i < P; i++) {
            Packages_Coeff[i] = 1;
        }

        //Array of length D (number of DAs)
        int[] Path_Total_Coeff = new int[D];
        for(int i = 0; i < D; i++) {
            Path_Total_Coeff[i] = 1;
        }

        //Constraints
        //Each Package Must Be Assigned Once
        for(int i = 0; i < P; i++) {
            model.sum(Packages[i], "=", 1).post();
        }

        for(int i = 0; i < D; i++) {
            BoolVar[] column = new BoolVar[P];
            for(int j = 0; j < P; j++) {
                column[j] = Packages[j][i];
            }
            //This calculates the total weight of packages assigned to DA i
            model.scalar(column, weight, "=", Tot_Weights[i]).post();

            //Total number of packages assigned to DA i
            model.scalar(column, Packages_Coeff, "=", Tot_Packages[i]);

            //Total Weight of DA i, cannot exceed capacity of DA i
            model.arithm(Tot_Weights[i], "<=", da_capacity[i]).post();

            //This calculates the total rough path of packages assigned to DA i
            model.scalar(column, roughDistances, "=", Tot_RoughPath[i]).post();

            //Naive constraint
            //Helps better spread the packages among the DAs, works only when DAs have same or very similar capacities
            //model.arithm(Tot_Weights[i], ">=", averageWeightPerDA).post();

            //This constraint limits the number of packages a DA can be assigned to 3.
            //If we want to implement limits on the number of packages a DA can hold, we can replace the three with a value pertaining to each DA
            //model.sum(column, "=", 3).post();
        }

        //Sum of all Tot_RoughPath variables into the Path_Total variable
        model.scalar(Tot_RoughPath, Path_Total_Coeff, "=", Path_Total).post();

        //The Solver
        //TODO: Expand this code so that:
        // More than one solution is looked at
        // This behaviour terminates if there is no valid solution

        //TODO: Change this to get Best
        Solver solver = model.getSolver();
        //Not working at the moment
        //Solution solution = solver.findOptimalSolution(Path_Total, false);

        Solution solution = solver.findSolution();

        //TODO: Replace this with the in-built choco function that determines if a solution cannot be found
        // Ideally, we should check total weight of packages compared to total capacity of DA's, etc before we get to the solver
        if(solution == null) return false;

        //NOTE:
        //This Code uses a Choco Solution saved into a variable called solution
        //When we expand the code so that multiple solutions are compared, save the best one into the solution variable,
        //so none of this code needs to be refactored

        //For each Delivery Agent
        //Processed in the order they appear in the agents ArrayList
        for(int i = 0; i < D; i++) {
            //Temp inventory, used to store copies of items before serialization
            Inventory inv = new Inventory();
            //Adding + 1 to i, so that this output matches the DA's LocalNames
            System.out.print("Delivery Agent " + (i + 1) + ": ");
            for(int j = 0; j < P; j++) {
                System.out.print( " Package " + j + " - ");
                //If a DA has been assigned a package, add it to the temp inventory
                if(solution.getIntVal(Packages[j][i]) == 1) {
                    System.out.print("Y");
                    inv.addItem(masterInventory.getItems().get(j));
                }
                else {
                    System.out.print("N");
                }
            }
            System.out.println( " Total Weight: " + solution.getIntVal(Tot_Weights[i]) + ".");

            //If no packages have been assigned to a DA, then nothing should be assigned to its AgentData
            if(!inv.isEmpty()) {
                //The temp inventory is serialized and added to the AgentData
                agents.get(i).setJsonInventory(inv.serialize());

                //Inventories are Sorted, and Paths are created from sorted inventories
                //Debug
                System.out.print("Testing Pre Order - ");
                for(Item item: inv.getItems()) {
                    System.out.print("Item " + item.getId() + ": Dest " + item.getDestination() + " ");
                }
                System.out.println(" Total Path Length: " + getPathLength(inv, agents.get(i).getCurrentLocation()));

                //TODO: Decide if Map Nodes start indexing at 0 or 1.
                // This code assumes Nodes start indexing at 0.
                //Second temp inventory, which items are added to in sorted order
                Inventory pathInv = sortInventory(inv, agents.get(i).getCurrentLocation());

                //Debug
                System.out.print("Testing Post Order - ");
                for(Item item: pathInv.getItems()) {
                    System.out.print("Item " + item.getId() + ": Dest " + item.getDestination() + " ");
                }
                System.out.println(" Total Path Length: " + getPathLength(pathInv, agents.get(i).getCurrentLocation()));

                //Iterates through the ordered items in pathInv
                //For each node between the item and previous item
                //The node ids are added to the loc ArrayList
                //The distances are added to the dist ArrayList
                ArrayList<Integer> loc = new ArrayList<>();
                ArrayList<Integer> dist = new ArrayList<>();
                int prev_loc = agents.get(i).getCurrentLocation();
                for(Item item: pathInv.getItems()) {
                    if(item.getDestination() != prev_loc) {
                        int[] next_dest = mapPaths[prev_loc][item.getDestination()];
                        for(int o = 0; o < next_dest.length; o++) {
                            loc.add(next_dest[o]);
                            dist.add(mapData[prev_loc][o]);
                            prev_loc = next_dest[o];
                        }
                    }
                }

                //The loc and dist ArrayLists are converted to arrays
                //These arrays are used to create a Path object
                //This path is serialized and added to the AgentData object
                int[] loc_array = loc.stream().mapToInt(o -> o).toArray();
                int[] dist_array = dist.stream().mapToInt(o -> o).toArray();
                Path path = new Path(loc_array, dist_array);
                agents.get(i).setJsonPath(path.serialize());
            }
        }*/
        return true;
    }

    //Parameters
    //Inventory inv: an Inventory Object
    //int startLocation: The currentLocation of the DA assigned to this inventory
    //
    //Sorts the Inventory into an order that creates the shortest possible path
    //
    //As items are sorted into order, they are removed from inv, and added to sortedInv
    //
    //Steps
    //-Starts a for loop, that loops once for each item in the inventory (loop i)
    //-In each loop:
    //      -Loops through the entire (remaining) inventory (loop j)
    //      -Using mapDist, checks the distance between the last sorted item's destination, and the destination of each item left in the inventory
    //      -The first item(i == 0), is compared against startLocation.
    //      -If shorter, or unassigned (closestDist == -1), then the new closestDist is saved, as well as the index of the item (closestItem)
    //      -At the end of loop j, the closestDist variable is reset, and the item at the index of (closestItem) is removed from inv, and added to pathInv
    public Inventory sortInventory(Inventory inv, int startLocation) {

        Inventory sortedInv = new Inventory();

        if(!inv.isEmpty()){
            {
                try {
                    int invLength = inv.getLength();

                    //Just to reduce the number of calls to inv.getItems();
                    ArrayList<Item> items;

                    int closestDist = -1;
                    int closestItem = 0;

                    for(int i = 0; i < invLength; i++) {

                        items = inv.getItems();

                        for(int j = 0; j < items.size(); j++) {
                            if(i == 0) {
                                if(mapDist[startLocation][items.get(j).getDestination() - 1] < closestDist || closestDist == -1) {
                                    closestDist = mapDist[startLocation][items.get(j).getDestination() - 1];
                                    closestItem = j;
                                }
                            }
                            else {
                                if(mapDist[sortedInv.getItems().get(i - 1).getDestination() - 1][items.get(j).getDestination() - 1] < closestDist || closestDist == -1) {
                                    closestDist = mapDist[sortedInv.getItems().get(i - 1).getDestination() - 1][items.get(j).getDestination() - 1];
                                    closestItem = j;
                                }
                            }
                        }
                        sortedInv.addItem(items.get(closestItem));
                        inv.removeItem(items.get(closestItem).getId());
                        closestDist = -1;
                    }

                    return sortedInv;

                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Index Was Out of Bounds, While Sorting Inventory");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("Sorting Inventory Caused an Exception");
                    e.printStackTrace();
                }
            }
        } else return inv;

        return sortedInv;
    }

    //Parameters
    //Inventory inv: Inventory Object
    //int startLocation: The node to start the path at
    //
    //Steps
    //  -Loops through the supplied inventory
    //  -Adds the distance between the destination of the current Item, and the previous item (startLocation is used for the first item)
    //  -Returns the total
    public int getPathLength(Inventory inv, int startLocation) {

        int result = 0;

        if(!inv.isEmpty()) {

            ArrayList<Item> items = inv.getItems();

            try {
                for(int i = 0; i < items.size(); i++) {
                    if (i == 0) {
                        result += mapDist[startLocation][items.get(i).getDestination() - 1];
                    }
                    else {
                        result += mapDist[items.get(i - 1).getDestination() - 1][items.get(i).getDestination() - 1];
                    }
                }

                return result;

            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Index Was Out of Bounds, While Finding Path Length");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Finding Path Length Caused Exception");
                e.printStackTrace();
            }
        } else return 0;

        return result;
    }

    //Loops through each of the Map Data arrays, and outputs them to console
    public void testMapData() {
        for(int i = 0; i < mapData.length; i++) {
            for(int j = 0; j < mapData[i].length; j++) {
                System.out.print(mapData[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println();

        for(int i = 0; i < mapDist.length; i++) {
            for(int j = 0; j < mapDist[i].length; j++) {
                System.out.print(mapDist[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println();
        for(int i = 0; i < mapPaths.length; i++) {
            for(int j = 0; j < mapPaths[i].length; j++) {
                System.out.print("{");
                for(int k = 0; k < mapPaths[i][j].length; k++) {
                    System.out.print(mapPaths[i][j][k] + ",");
                }
                System.out.print("}");
            }
            System.out.println();
        }
    }

    //Overriding of MyAgentInterface Methods
    @Override
    public void StartMasterAgent() {
        if(!processing) {
            addBehaviour(new processRoutes());
            addBehaviour(new listenForMessages());
        }
    }

    @Override
    public boolean AddItemToInventory(Item i) {
        return masterInventory.addItem(i);
    }

    @Override
    public void GenerateMap(int v, int dMin, int dMax, int eMin, int eMax) {
        if(graph == null) {
            graph = GraphGen.autoGenerate(v, dMin, dMax, eMin, eMax);

            mapData = graph.getMapData();
            mapDist = graph.getMapDist();
            mapPaths = graph.getMapPaths();

            System.out.println(getLocalName() + ": Map Generated!");
            testMapData();
            generateItems();
        }
        else {
            System.out.println(getLocalName() + ": Map Already Generated!");
        }
    }

    @Override
    public void OverwriteOutput(OutputStream out) {
        System.setOut(new PrintStream(out, true));
    }

    private void generateItems() {

        //TODO: pass the DA capacities to this method
        int maxCapacitiesOfAllDAs = 300;

        int itemID = 1;
        String itemName = "Item";
        String tempName = itemName;
        int destination = 0;
        int weight = 0;
        int size = 1;

        int numberOfLocations = mapDist.length;

        while (maxCapacitiesOfAllDAs >= 10) {
            destination = (int) (Math.random() * ((numberOfLocations - 1) + 1)) + 1;
            weight = (int) (Math.random() * ((10 - 1) + 1)) + 1;
            tempName = itemName + Integer.toString(itemID);
            System.out.println("ITEM NAME IS: " + tempName + ", ID: " + itemID);
            masterInventory.addItem(new Item(itemID, tempName, destination, weight, size));
            itemID++;
            maxCapacitiesOfAllDAs -= weight;
        }
    }
}