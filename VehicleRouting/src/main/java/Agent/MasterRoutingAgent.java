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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
<<<<<<< HEAD
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.tools.ArrayUtils;
=======
>>>>>>> 0b2e6dca48d21871d803100f8057163a0df02d15


import java.util.ArrayList;

import static org.chocosolver.util.tools.StatisticUtils.sum;

public class MasterRoutingAgent extends Agent {
    //Collection of AgentData Objects, to keep track of the state of each DA this Agent is aware of
    private ArrayList<AgentData> agents = new ArrayList<>();

    //Initial storage place of packages to be delivered. Once a package is sent to a DA, it is removed from this list,
    //and added to the Individual Inventory objects contained in each AgentData object
    private Inventory masterInventory = new Inventory();

    //Initial storage place of paths to be sent to DAs. Paths will likely be created based on the CSP solver, so this list will likely not be used, and can be removed
    private ArrayList<Path> paths = new ArrayList<>();

    //Map Data
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

        //TODO: Remove this Dummy Data
        //Dummy Item Data
        masterInventory.addItem(new Item(1, "Item1", 2, 3, 1));
        masterInventory.addItem(new Item(2, "Item2", 3, 5, 1));
        masterInventory.addItem(new Item(3, "Item3", 2, 7, 1));
        masterInventory.addItem(new Item(4, "Item4", 4, 4, 1));
        masterInventory.addItem(new Item(5, "Item5", 1, 2, 1));
        masterInventory.addItem(new Item(6, "Item6", 2, 10, 1));
        masterInventory.addItem(new Item(7, "Item7", 1, 8, 1));
        masterInventory.addItem(new Item(8, "Item8", 2, 6, 1));
        masterInventory.addItem(new Item(9, "Item9", 4, 15, 1));
        masterInventory.addItem(new Item(10, "Item10", 2, 10, 1));
        masterInventory.addItem(new Item(11, "Item11", 3, 15, 1));
        masterInventory.addItem(new Item(12, "Item12", 2, 17, 1));
        masterInventory.addItem(new Item(13, "Item13", 4, 9, 1));
        masterInventory.addItem(new Item(14, "Item14", 1, 1, 1));
        masterInventory.addItem(new Item(15, "Item15", 2, 7, 1));
        masterInventory.addItem(new Item(16, "Item16", 1, 3, 1));
        masterInventory.addItem(new Item(17, "Item17", 2, 16, 1));
        masterInventory.addItem(new Item(18, "Item18", 4, 5, 1));

        //TODO: Replace this with the GraphGen code
        //Dummy Map Data
        //MapData
        mapData = new int[][]{{0, 1, 0, 0, 3},
                              {1, 0, 4, 0, 0},
                              {0, 4, 0, 1, 0},
                              {0, 0, 1, 0, 2},
                              {3, 0, 0, 2, 0}};

        //MapDist
        mapDist = new int[][]{{0, 1, 5, 5, 3},
                              {1, 0 ,4, 5, 4},
                              {5, 4, 0, 1, 3},
                              {5, 5, 1, 0, 2},
                              {3, 4, 3, 2, 0}};

        //MapPath
        mapPaths = new int[][][]{{{}, {1}, {1, 2}, {4, 3}, {4}},
                                 {{0}, {}, {2}, {2, 3}, {0, 4}},
                                 {{1, 0}, {1}, {}, {3}, {3, 4}},
                                 {{4, 0}, {2, 1}, {2}, {}, {4}},
                                 {{0}, {0, 1}, {3, 2}, {3}, {}}};

        //Sleeping, To Give Jade time to start up.
        //Probably can remove, once the graph generation stuff is hooked up as it will cause enough delay that this isn't needed
        //Secondly, the GUI will not be interactible until after this is created, so this is probable unnecessary once it is hooked up to the GUI
        try{
            Thread.sleep(2000);
        }catch(Exception ex){System.out.println("Sleeping caused an error");}

        addBehaviour(new processRoutes());
    }

    //TODO:
    // Add in message that nullifies the inventories and paths of each agentdata, once the DA sends a path complete message
    // Additionally, when the DA sends a path complete message, send it a path that leads it back to node 0, and send it a message to start.
    //Behaviour that constantly loops, listening for messages
    //Works in the same way as the ListenForMessages behaviour in the DeliveryAgent class
    //
    //This behaviour is only added when the ProcessRoutes behaviour has completed.
    //Should the ProcessRoutes behaviour be required a second time, this behaviour will need to be blocked/removed.
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

    //TODO: Make sure an AgentData object for a particular agent doesn't already exist, when populating the agents ArrayList in step 0
    //Behaviour that constantly loops its action() method, until the done() method returns true
    //What is run in the action() method, depends on the value of the step variable
    //
    //The Steps
    //Step 0: Find Agents, and request their status
    //  -Using the AMSService, an AID list of all Agents on the System is returned
    //  -The list of Agents is looped through
    //  -If any Agent names contain "DeliveryAgent", a new AgentData object is created using the AID of the agent
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
    //  -The done variable is set to true
    //  -The ListenForMessages behaviour is added
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

                    //TODO: Terminate here if total weight of packages exceeds total capacity of all delivery agents


                    //TODO: Expand CSP Solver
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

                    //Number of Delivery Agents
                    int D = agents.size();

                    //Carrying Capacity of each Delivery Agent
                    int[] da_capacity;
                    for(AgentData agent: agents) {
                        temp.add(agent.getCapacity());
                    }
                    da_capacity = temp.stream().mapToInt(o -> o).toArray();
                    temp.clear();

                    Model model = new Model("Vehicle Routing Solver");

                    //Variables
                    //Boolean Variable for Each Combination of Package and DA
                    //If a variable is true, it means that DA is the delivery agent for that package
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
                        Tot_Weights[i] = model.intVar("DA " + i + "Capacity", 0, 100);
                    }

                    IntVar[] LeftOverCapacity = new IntVar[D];
                    for(int i = 0; i < D; i++) {
                        LeftOverCapacity[i] = model.intVar("DA " + i + "Left Over Capacity", -100, 100);
                    }

                    IntVar sum = model.intVar(0, 99999);

                    //Constraints
                    //Each Package Must Be Assigned Once
                    for(int i = 0; i < P; i++) {
                        model.sum(Packages[i], "=", 1).post();
                    }

                    //The average weight of the parcels is sum of the weights of all the parcels divided by the number of delivery agents
                    //This is based on the naive assumption that all the DAs have the same capacity (or at least very similar)
                    int averageWeightPerDA = sum(weight) / D;

                    for(int i = 0; i < D; i++) {
                        BoolVar[] column = new BoolVar[P];
                        for(int j = 0; j < P; j++) {
                            column[j] = Packages[j][i];
                        }
                        //This calculates the total weight of packages assigned to DA i
                        model.scalar(column, weight, "=", Tot_Weights[i]).post();

                        int temp2 = Tot_Weights[i].getValue();
                        int temp3 = da_capacity[i] - temp2;
                        LeftOverCapacity[i] = model.intVar(temp3);

                        //This doesn't work, makes no difference to the solution found.
                        //model.setObjective(true, LeftOverCapacity[i]);

                        //Total Weight of DA i, cannot exceed capacity of DA i
                        model.arithm(Tot_Weights[i], "<=", da_capacity[i]).post();

                        //NAIVE CONSTRAINT
                        //For every DA, the sum of its packages weight must be equal or greater than the average
                        //This gives us a decent solution when all DAs have similar capacity
                        model.arithm(Tot_Weights[i], ">=", averageWeightPerDA).post();

                        //This constraint limits the number of packages a DA can be assigned to 3.
                        //If we want to implement limits on the number of packages a DA can hold, we can replace the three with a value pertaining to each DA
                        //model.sum(column, "=", 3).post();
                    }

                    //The Solver
                    //TODO: Expand this code so that:
                    // More than one solution is looked at
                    // This behaviour terminates if there is no valid solution
                    Solver solver = model.getSolver();
                    solver.showStatistics();

                    Solution solution = solver.findSolution();

                    //This will maximize the left over capacity for any one DA
//                    while(solver.solve()) {
//                        solution = solver.findOptimalSolution(LeftOverCapacity[2], true);
//                    }

                    for(int i = 0; i < D; i++) {
                        Inventory inv = new Inventory();
                        //Adding + 1 to i, so that this output matches the DA's LocalNames
                        System.out.print("Delivery Agent " + (i + 1) + ": ");
                        for(int j = 0; j < P; j++) {
                            System.out.print( " Package " + j + " - ");
                            if(solution.getIntVal(Packages[j][i]) == 1) {
                                System.out.print("Y");
                                inv.addItem(masterInventory.getItems().get(j));
                            }
                            else {
                                System.out.print("N");
                            }
                        }
                        System.out.println( " Total Weight: " + solution.getIntVal(Tot_Weights[i]) + ".");

                        //If no packages have been assigned to a DA, then nothing should be assigned to its agentdata
                        if(!inv.isEmpty()) {
                            agents.get(i).setJsonInventory(inv.serialize());

                            //TODO: Clean up this code, it is an absolute mess
                            //Sort Items into Order
                            System.out.print("Testing Pre Order - ");
                            for(Item item: inv.getItems()) {
                                System.out.print(item.getId() + ":" + item.getDestination() + " ");
                            }
                            System.out.println();

                            Inventory pathInv = new Inventory();

                            int l = inv.getLength();
                            int m = -1;
                            int n = 0;

                            //TODO: Decide if Map Nodes start indexing at 0 or 1.
                            // This code assumes Nodes start indexing at 0.
                            for(int j = 0; j < l; j++) {
                                for(int k = 0; k < inv.getLength(); k++) {
                                    if(j == 0) {
                                        if(m == -1) {
                                            m = mapDist[agents.get(i).getCurrentLocation()][inv.getItems().get(k).getDestination()];
                                            n = k;
                                        } else if(mapDist[agents.get(i).getCurrentLocation()][inv.getItems().get(k).getDestination()] < m) {
                                            m = mapDist[agents.get(i).getCurrentLocation()][inv.getItems().get(k).getDestination()];
                                            n = k;
                                        }
                                    }
                                    else {
                                        if(m == -1) {
                                            m = mapDist[pathInv.getItems().get(j - 1).getDestination()][inv.getItems().get(k).getDestination()];
                                            n = k;
                                        } else if(mapDist[pathInv.getItems().get(j - 1).getDestination()][inv.getItems().get(k).getDestination()] < m) {
                                            m = mapDist[pathInv.getItems().get(j - 1).getDestination()][inv.getItems().get(k).getDestination()];
                                            n = k;
                                        }
                                    }
                                }
                                m = -1;
                                pathInv.addItem(inv.getItems().get(n));
                                inv.removeItem(inv.getItems().get(n).getId());
                            }

                            System.out.print("Testing Post Order - ");
                            for(Item item: pathInv.getItems()) {
                                System.out.print(item.getId() + ":" + item.getDestination() + " ");
                            }
                            System.out.println();

                            //Assemble Locations and Distances
                            ArrayList<Integer> loc = new ArrayList<>();
                            ArrayList<Integer> dist = new ArrayList<>();
                            int prev_loc = agents.get(i).getCurrentLocation();
                            for(Item item: pathInv.getItems()) {
                                if(item.getDestination() != prev_loc) {
                                    int[] next_dest = mapPaths[prev_loc][item.getDestination()];
                                    for(int o = 0; o < next_dest.length; o++) {
                                        loc.add(next_dest[o]);
                                        dist.add(mapDist[prev_loc][o]);
                                        prev_loc = next_dest[o];
                                    }
                                }
                            }

                            //Create, Serialise and add to DA
                            int[] loc_array = loc.stream().mapToInt(o -> o).toArray();
                            int[] dist_array = loc.stream().mapToInt(o -> o).toArray();
                            Path path = new Path(loc_array, dist_array);
                            agents.get(i).setJsonPath(path.serialize());
                        }

                    //Solve the constraint problem, and terminate if no solution is found
                    if(!solveConstraintProblem()) {
                        System.out.println(getLocalName() + ": No Solution Found. Stopping this Behaviour");
                        done = true;

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
                                    //I don't think we need to keep track of the Delivery Agents Path, so this is ultimately unnecessary
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
                        //If this behaviour ever needs to be rerun, this for loop will need to be altered
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
                    throw new RuntimeException(getLocalName() + ": processRoutes is at an invalid step");
            }
        }

        public boolean done() {
            return done;
        }
    }

    //Function for solving the CSP problem, and processing the solution
    //This function is complicated, so it is commented throughout
    //TODO: Add a proper writeup of this function, once the code has been cleaned up properly
    //Assigns json representation of paths and inventories to AgentData objects in the agents ArrayList
    //Returns if a solution is found and processed, false otherwise
    public boolean solveConstraintProblem() {
        //TODO: Expand CSP Solver
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

        //Number of Delivery Agents
        int D = agents.size();

        //Carrying Capacity of each Delivery Agent
        int[] da_capacity;
        for(AgentData agent: agents) {
            temp.add(agent.getCapacity());
        }
        da_capacity = temp.stream().mapToInt(o -> o).toArray();
        temp.clear();

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
            Tot_Weights[i] = model.intVar("DA " + i + "Capacity", 0, IntVar.MAX_INT_BOUND);
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

            //Total Weight of DA i, cannot exceed capacity of DA i
            model.arithm(Tot_Weights[i], "<=", da_capacity[i]).post();

            //This constraint limits the number of packages a DA can be assigned to 3.
            //If we want to implement limits on the number of packages a DA can hold, we can replace the three with a value pertaining to each DA
            //model.sum(column, "=", 3).post();
        }

        //The Solver
        //TODO: Expand this code so that:
        // More than one solution is looked at
        // This behaviour terminates if there is no valid solution
        Solver solver = model.getSolver();
        Solution solution = solver.findSolution();

        //TODO: Clean this up properly
        if(solution == null) return false;

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

                //TODO: Clean up this code, it is an absolute mess
                //Sort Items into Order
                //Items are ordered by which item's destination is closest to the destination of the previous item
                //The DA's currentLocation is used for the first item

                //Debug
                System.out.print("Testing Pre Order - ");
                for(Item item: inv.getItems()) {
                    System.out.print("Item " + item.getId() + ": Dest " + item.getDestination() + " ");
                }
                System.out.println();

                //Second temp inventory, which items are added to in sorted order
                Inventory pathInv = new Inventory();

                int l = inv.getLength();
                int m = -1;
                int n = 0;

                //TODO: Decide if Map Nodes start indexing at 0 or 1.
                // This code assumes Nodes start indexing at 0.
                //Finds which item is closest, and adds it to the pathInv
                for(int j = 0; j < l; j++) {
                    for(int k = 0; k < inv.getLength(); k++) {
                        if(j == 0) {
                            if(m == -1) {
                                m = mapDist[agents.get(i).getCurrentLocation()][inv.getItems().get(k).getDestination()];
                                n = k;
                            } else if(mapDist[agents.get(i).getCurrentLocation()][inv.getItems().get(k).getDestination()] < m) {
                                m = mapDist[agents.get(i).getCurrentLocation()][inv.getItems().get(k).getDestination()];
                                n = k;
                            }
                        }
                        else {
                            if(m == -1) {
                                m = mapDist[pathInv.getItems().get(j - 1).getDestination()][inv.getItems().get(k).getDestination()];
                                n = k;
                            } else if(mapDist[pathInv.getItems().get(j - 1).getDestination()][inv.getItems().get(k).getDestination()] < m) {
                                m = mapDist[pathInv.getItems().get(j - 1).getDestination()][inv.getItems().get(k).getDestination()];
                                n = k;
                            }
                        }
                    }
                    m = -1;
                    pathInv.addItem(inv.getItems().get(n));
                    inv.removeItem(inv.getItems().get(n).getId());
                }

                //Debug
                System.out.print("Testing Post Order - ");
                for(Item item: pathInv.getItems()) {
                    System.out.print("Item " + item.getId() + ": Dest " + item.getDestination() + " ");
                }
                System.out.println();

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
                int[] dist_array = loc.stream().mapToInt(o -> o).toArray();
                Path path = new Path(loc_array, dist_array);
                agents.get(i).setJsonPath(path.serialize());
            }
        }
        return true;
    }
}
