package Agent;


import Agent.AgentInfo.AgentData;
import Communication.Message;
import DeliveryPath.Path;
import GUI.MyAgentInterface;
import Item.Inventory;
import Item.Item;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class MasterRoutingAgent extends Agent implements MyAgentInterface {
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
        registerO2AInterface(MyAgentInterface.class, this);
    }

    private Inventory masterInventory = new Inventory();
    private int[][] mapData;
    private ArrayList<Path> paths = new ArrayList<>();

    private class processRoutes extends Behaviour {

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
                    System.out.println("Step 0");

                    //Find all Delivery Agents with AMS
                    //Send them a request for their information
                    AMSAgentDescription[] a = null;

                    try {
                        SearchConstraints c = new SearchConstraints();
                        c.setMaxResults((long) -1);
                        a = AMSService.search(myAgent, new AMSAgentDescription(), c);
                    } catch (Exception ex) {
                        System.out.println(myAgent.getLocalName() + ": ERROR in Finding Delivery Agents" + ex );
                        ex.printStackTrace();
                    }

                    //TODO: Fix this jank
                    for(int i = 0; i < a.length; i++) {
                        if(a[i].getName().toString().contains("DeliveryAgent")) {
                            agents.add(new AgentData(a[i].getName()));
                        }
                    }

                    System.out.println(getLocalName() + ": Found " + agents.size() + " Agents");

                    for(AgentData agent: agents) {
                        System.out.println(agent.getName());
                    }

                    if(agents.size() > 0) {
                        ACLMessage capacity_request = new ACLMessage(ACLMessage.REQUEST);
                        for(AgentData agent: agents) {
                            capacity_request.addReceiver(agent.getName());
                        }
                        capacity_request.setContent(Message.CAPACITY);
                        capacity_request.setConversationId("processRoute");
                        capacity_request.setReplyWith("Request" + System.currentTimeMillis());
                        myAgent.send(capacity_request);

                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(capacity_request.getReplyWith()));

                        step = 1;
                    }
                    else {
                        System.out.println("No Agents Found");
                        done = true;
                    }

                    break;

                case 1:
                    System.out.println("Step 1");
                    ACLMessage capacity_response = myAgent.receive(mt);
                    if(capacity_response != null) {

                        if(capacity_response.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(capacity_response.getSender())) {
                                    agent.setCapacity(Integer.parseInt(capacity_response.getContent()));
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + capacity_response.getSender().toString() + " supplied an invalid capacity");
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }

                        replyCount++;
                        System.out.println(getLocalName() + ": Received " + replyCount + " replies out of " + agents.size());
                        if(replyCount >= agents.size()) {
                            replyCount = 0;
                            step = 2;
                        }
                    }
                    else {
                        block();
                        System.out.println(myAgent.getLocalName() + ": Blocking in Step 1");
                    }
                    break;

                case 2:
                    System.out.println("Step 2");
                    //TODO: CREATE DUMMY INVENTORY DATA
                    Item item1 = new Item(1, "Item1", 2, 1, 1);
                    Item item2 = new Item(2, "Item2", 5, 1, 1);
                    Item item3 = new Item(3, "Item3", 7, 1, 1);
                    Item item4 = new Item(4, "Item4", 6, 1, 1);
                    Item item5 = new Item(5, "Item5", 1, 1, 1);
                    Item item6 = new Item(6, "Item6", 9, 1, 1);
                    Item item7 = new Item(7, "Item7", 3, 1, 1);
                    Item item8 = new Item(8, "Item8", 3, 1, 1);
                    Item item9 = new Item(9, "Item9", 6, 1, 1);

                    Inventory i1 = new Inventory();
                    i1.addItem(item1);
                    i1.addItem(item2);
                    i1.addItem(item3);

                    Inventory i2 = new Inventory();
                    i2.addItem(item4);
                    i2.addItem(item5);
                    i2.addItem(item6);

                    Inventory i3 = new Inventory();
                    i3.addItem(item7);
                    i3.addItem(item8);
                    i3.addItem(item9);


                    //TODO: CREATE DUMMY PATH DATA
                    Path p1 = new Path(new int[]{1,3,5,4,7}, new int[]{4,3,2,5,2});
                    Path p2 = new Path(new int[]{6,1,2,9}, new int[]{2,5,3,1});
                    Path p3 = new Path(new int[]{2,3,6}, new int[]{5,3,6});

                    //Add Inventories and Paths to the AgentData Objects

                    Path[] paths = {p1, p2, p3};
                    Inventory[] inventories = {i1, i2, i3};

                    int i = 0;
                    for(AgentData agent: agents) {
                        agent.setJsonInventory(inventories[i].serialize());
                        System.out.println(agent.getJsonInventory());
                        agent.setJsonPath(paths[i].serialize());
                        System.out.println(agent.getJsonPath());
                        i++;
                    }

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
                            System.out.println(inventory_add.getContent());
                            myAgent.send(inventory_add);
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + agent.getName().toString() + " has an empty json inventory string");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    step = 3;

                    break;

                case 3:
                    System.out.println("Step 3");
                    //Process Inventory Replies
                    ACLMessage inventory_response = myAgent.receive(mt);
                    if(inventory_response != null) {

                        if(inventory_response.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(inventory_response.getSender())) {
                                    //Set the inventory in AgentData Object here, but we don't need to do anything in here for now.
                                }
                            }
                            if(inventory_response.getContent().equals(Message.INVENTORY_SUCCESS)) {
                                //Do Nothing, this is what we want
                            }
                            else if(inventory_response.getContent().equals(Message.INVENTORY_FAILURE)) {
                                try {
                                    throw new Exception(myAgent.getLocalName() + ": ERROR - " + inventory_response.getSender().toString() + " could not load supplied inventory");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + inventory_response.getSender().toString() + " replied with incorrect performative");
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }

                        replyCount++;
                        if(replyCount >= agents.size()) {
                            replyCount = 0;
                            step = 4;
                        }
                    }
                    else {
                        block();
                        System.out.println(myAgent.getLocalName() + ": Blocking in Step 3");
                    }
                    break;

                case 4:
                    System.out.println("Step 4");
                    //Send Paths to all DAs
                    //Set up Message
                    ACLMessage path_add = new ACLMessage(ACLMessage.INFORM);
                    path_add.setConversationId("processRoute");
                    path_add.setReplyWith(Message.PATH + System.currentTimeMillis());
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(path_add.getReplyWith()));

                    //Send Inventory to Each Agent
                    for(AgentData agent: agents) {
                        if(!agent.getJsonInventory().isEmpty()) {
                            path_add.clearAllReceiver();
                            path_add.addReceiver(agent.getName());
                            path_add.setContent(Message.PATH + ":" + agent.getJsonPath());
                            myAgent.send(path_add);
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + agent.getName().toString() + " has an empty json path string");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    step = 5;
                    break;

                case 5:
                    System.out.println("Step 5");
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
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + path_response.getSender().toString() + " replied with incorrect performative");
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }

                        replyCount++;
                        if(replyCount >= agents.size()) {
                            replyCount = 0;
                            step = 6;
                        }
                    }
                    else {
                        block();
                        System.out.println(myAgent.getLocalName() + ": Blocking in Step 5");
                    }
                    break;

                case 6:
                    System.out.println("Step 6");
                    //Tell all DAs to Start
                    ACLMessage start = new ACLMessage(ACLMessage.REQUEST);
                    for(AgentData agent: agents) {
                        start.addReceiver(agent.getName());
                    }
                    start.setContent(Message.START);
                    myAgent.send(start);

                    done = true;
                    
                    break;

                default:
                    throw new RuntimeException("beginRouting is at an invalid step");
            }
        }

        public boolean done() {
            return done;
        }
    }
    @Override
    public void StartMasterAgent() {
        addBehaviour(new processRoutes());
    }

}
