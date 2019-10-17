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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class MasterRoutingAgent extends Agent {
    private final int vertices;
    private static final int NO_PARENT = -1;

    //Collection of AgentData Objects, to keep track of the state of each DA this Agent is aware of
    private ArrayList<AgentData> agents = new ArrayList<>();

    //Initial storage place of packages to be delivered. Once a package is sent to a DA, it is removed from this list
    private Inventory masterInventory = new Inventory();

    //Initial storage place of paths to be sent to DAs. Paths will likely be created based on the CSP solver, so this list will likely not be used.
    private ArrayList<Path> paths = new ArrayList<>();

    //Map Data
    //2D Array of Actual Connections between nodes.
    //A value of 0 means there is no direct connection.
    private int[][] mapData;

    //2D Array, which contains the distance of the shortest path between any two nodes
    //eg; mapDist[i][j] = 5, means the shortest path between nodes i and j is 5.
    private int[][] mapDist;

    //2D Array, which contains additional arrays, which contain the actual nodes required to travel between any 2 nodes.
    //Exclusive of first node, but inclusive of second node.
    //eg; mapPaths[i][j] = {k, l, j}.
    private int[][][] mapPaths;


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

    public GraphGen(int v)
    {
        vertices = v + 1;
        mapData = new int[vertices][vertices];
        mapDist = new int[vertices][vertices];
        mapPaths = new int[vertices][vertices][];
    }

    public void makeEdge(int to, int from, int edge)
    {
        try
        {
            mapData[to][from] = edge;
            mapData[from][to] = edge;
        }
        catch (ArrayIndexOutOfBoundsException index)
        {
            System.out.println("The vertices does not exists");
        }
    }
    public int getEdge(int to, int from)
    {
        try
        {
            return mapData[to][from];
        }
        catch (ArrayIndexOutOfBoundsException index)
        {
            System.out.println("The vertices does not exists");
        }
        return 0;
    }

    int getMinimumVertex(boolean [] mst, int [] key){
        int minKey = Integer.MAX_VALUE;
        int vertex = -1;
        for (int i = 1; i <vertices; i++) {
            if(mst[i]==false && minKey>key[i]){
                minKey = key[i];
                vertex = i;
            }
        }
        return vertex;
    }

    private static void dijkstra(int[][] adjacencyMatrix, int startVertex, int endVertex) {
        int nVertices = adjacencyMatrix[0].length - 1;
        System.out.println("nVertices: " + nVertices);
        // shortestDistances[i] will hold the
        // shortest distance from src to i
        int[] shortestDistances = new int[nVertices + 1];

        // added[i] will true if vertex i is
        // included / in shortest path tree
        // or shortest distance from src to
        // i is finalized
        boolean[] added = new boolean[nVertices + 1];

        // Initialize all distances as
        // INFINITE and added[] as false
        for (int vertexIndex = 1; vertexIndex <= nVertices; vertexIndex++) {
            shortestDistances[vertexIndex] = Integer.MAX_VALUE;
            added[vertexIndex] = false;
        }
        // Distance of source vertex from
        // itself is always 0
        shortestDistances[startVertex] = 0;

        // Parent array to store shortest
        // path tree
        int[] parents = new int[nVertices + 1];

        // The starting vertex does not
        // have a parent
        parents[startVertex] = NO_PARENT;

        // Find shortest path for all
        // vertices
        for (int i = 1; i <= nVertices; i++) {

            // Pick the minimum distance vertex
            // from the set of vertices not yet
            // processed. nearestVertex is
            // always equal to startNode in
            // first iteration.
            int nearestVertex = -1;
            int shortestDistance = Integer.MAX_VALUE;
            for (int vertexIndex = 1; vertexIndex <= nVertices; vertexIndex++) {
                if (!added[vertexIndex] && shortestDistances[vertexIndex] < shortestDistance) {
                    nearestVertex = vertexIndex;
                    shortestDistance = shortestDistances[vertexIndex];
                }
            }
            // Mark the picked vertex as
            // processed
            added[nearestVertex] = true;

            // Update dist value of the
            // adjacent vertices of the
            // picked vertex.
            for (int vertexIndex = 1; vertexIndex <= nVertices; vertexIndex++) {
                int edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex];
                if (edgeDistance > 0 && ((shortestDistance + edgeDistance) < shortestDistances[vertexIndex])) {
                    parents[vertexIndex] = nearestVertex;
                    shortestDistances[vertexIndex] = shortestDistance + edgeDistance;
                }
            }
        }
        printSolution(startVertex, endVertex, shortestDistances, parents);
    }

    private static void printSolution(int startVertex, int endVertex, int[] distances, int[] parents) {
        int nVertices = distances.length - 1;

        System.out.println("nVertices: " + nVertices);
        System.out.print("Vertex\t Distance\tPath");

        mapDist[startVertex][endVertex] = distances[endVertex];

        System.out.print("\n" + startVertex + " -> " + endVertex + " \t\t " + distances[endVertex] + "\t\t");
        String pathOutput = "";
        pathOutput = printPath(endVertex, parents, pathOutput);

        String[] pathTemp = pathOutput.split(",");
        int[] path = new int[pathTemp.length];
        //Iterate from 1, because the delivery agent assumes it is already at the first location.
        for(int i = 2; i < pathTemp.length; i++) {
            path[i] = Integer.parseInt(pathTemp[i]);
        }
        mapPaths[startVertex][endVertex] = path;

        for(int i = 2; i< pathTemp.length; i++){
            System.out.println("MAP PATH " + i + ": " + mapPaths[startVertex][endVertex][i]);
        }
    }

    // Function to print shortest path
    // from source to currentVertex
    // using parents array
    private static String printPath(int currentVertex, int[] parents, String pathOutput) {
        // Base case : Source node has
        // been processed
        if (currentVertex == NO_PARENT) {
            return "";
        }
        pathOutput = printPath(parents[currentVertex], parents, pathOutput);
        pathOutput += ("," + String.valueOf(currentVertex));
        //System.out.print(currentVertex + " ");
        return pathOutput;
    }

    class ResultSet{
        int parent;
        int weight;
    }

    public boolean primMST(){
        boolean[] mst = new boolean[vertices];
        ResultSet[] resultSet = new ResultSet[vertices];
        int [] key = new int[vertices];

        //Initialize all the keys to infinity and
        //initialize resultSet for all the vertices
        for (int i = 1; i <vertices; i++) {
            key[i] = Integer.MAX_VALUE;
            resultSet[i] = new ResultSet();
        }

        //start from the vertex 0
        key[1] = 0;
        resultSet[1] = new ResultSet();
        resultSet[1].parent = -1;

        //create MST
        for (int i = 1; i <vertices; i++) {

            //get the vertex with the minimum key
            int vertex = getMinimumVertex(mst, key);

            //include this vertex in MST
            mst[vertex] = true;

            //iterate through all the adjacent vertices of above vertex and update the keys
            for (int j = 1; j <vertices; j++) {
                //check of the edge
                if(mapData[vertex][j]>0){
                    //check if this vertex 'j' already in mst and
                    //if no then check if key needs an update or not
                    if(mst[j]==false && mapData[vertex][j]<key[j]){
                        //update the key
                        key[j] = mapData[vertex][j];
                        //update the result set
                        resultSet[j].parent = vertex;
                        resultSet[j].weight = key[j];
                    }
                }
            }
        }
        //print mst
        printMST(resultSet);
        return false;
    }

    public void printMST(ResultSet[] resultSet){
        int total_min_weight = 0;

        System.out.println("Minimum Spanning Tree: ");
        for (int i = 1; i <vertices; i++) {
            total_min_weight += resultSet[i].weight;
        }
        System.out.println("Total minimum key: " + total_min_weight);
    }

    public static int getNumNodes(){
        int v;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of nodes: ");
        v = sc.nextInt();
        return  v;
    }
    public static int getMaxCon(){
        int eMax;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the maximum number of connections: ");
        eMax = sc.nextInt();
        return  eMax;
    }
    public static int getMinCon(){
        int eMin;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the minimum number of connections: ");
        eMin = sc.nextInt();
        return  eMin;
    }
    public static int getMaxDist(){
        int dMax;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the maximum distance: ");
        dMax = sc.nextInt();
        return  dMax;
    }
    public static int getMinDist(){
        int dMin;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the minimum distance: ");
        dMin = sc.nextInt();
        return  dMin;
    }

    public static GraphGen generateGraph(int v, int eMin, int eMax, Random r, int dMin, int dMax){
        GraphGen graphTemp = new GraphGen(v);
        int vTo, vFrom, dRand, eRand, count = 1;
        eRand = ThreadLocalRandom.current().nextInt(eMin, eMax + 1);
        while (count <= eRand) {
            vTo = r.nextInt(v) + 1;
            vFrom = r.nextInt(v) + 1;
            dRand = ThreadLocalRandom.current().nextInt(dMin, dMax + 1);
            if (vTo != vFrom) {
                graphTemp.makeEdge(vTo, vFrom, dRand);
                count++;
            }
        }
        return graphTemp;
    }

    protected void setup() {
        System.out.println(getAID().getLocalName() + ": I have been created");

        //TODO: Remove this Dummy Data
        //Dummy Item Data
        masterInventory.addItem(new Item(1, "Item1", 2, 12, 1));
        masterInventory.addItem(new Item(2, "Item2", 3, 50, 1));
        masterInventory.addItem(new Item(3, "Item3", 2, 12, 1));
        masterInventory.addItem(new Item(4, "Item4", 4, 11, 1));
        masterInventory.addItem(new Item(5, "Item5", 1, 2, 1));
        masterInventory.addItem(new Item(6, "Item6", 2, 11, 1));
        masterInventory.addItem(new Item(7, "Item7", 1, 16, 1));
        masterInventory.addItem(new Item(8, "Item8", 2, 12, 1));
        masterInventory.addItem(new Item(9, "Item9", 4, 20, 1));

        int v, dMin, dMax, eMin, eMax;
        boolean disGraph = true;
        Random r = new Random();
        Scanner sc = new Scanner(System.in);
        GraphGen graph = null;
        try {
            v = getNumNodes();
            eMin = getMinCon();
            eMax = getMaxCon();

            if (eMin > eMax) {
                System.out.println("Minimum cannot be greater than Maximum");
            }
            dMin = getMinDist();
            dMax = getMaxDist();

            if (dMin > dMax) {
                System.out.println("Minimum cannot be greater than Maximum");
            }
            int failed_attempts = 0;
            while (disGraph){
                graph = generateGraph(v, eMin, eMax, r, dMin, dMax);
                try {
                    disGraph = graph.primMST();
                } catch (Exception E) {
                    failed_attempts++;
                    System.out.println("Graph was disconnected, Trying again: " + failed_attempts);
                    disGraph = true;
                }
            }
        } catch (Exception E) {
            System.out.println("Something went wrong");
        }
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
        try{
            Thread.sleep(2000);
        }catch(Exception ex){System.out.println("Sleeping caused an error");}

        addBehaviour(new processRoutes());
    }

    //TODO:
    // Add in message that nullifies the inventories and paths of each agentdata, once the DA sends a path complete message
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
