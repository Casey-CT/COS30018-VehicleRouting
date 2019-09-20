package Agent;


import Agent.AgentInfo.AgentData;
import Communication.Message;
import DeliveryPath.Path;
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

        try{
            Thread.sleep(5000);
        }catch(Exception ex){System.out.println("Sleeping caused an error");}

        addBehaviour(new processRoutes());
    }

    private Inventory masterInventory = new Inventory();
    private int[][] mapData;
    private ArrayList<Path> paths = new ArrayList<>();

    private class processRoutes extends Behaviour {

        //Number of received replies
        private int replyCount = 0;

        //Reusable Message
        private ACLMessage message;

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

                    if(agents.size() > 0) {
                        message = new ACLMessage(ACLMessage.REQUEST);
                        for(AgentData agent: agents) {
                            message.addReceiver(agent.getName());
                        }
                        message.setContent(Message.CAPACITY);
                        message.setConversationId("processRoute");
                        message.setReplyWith("Request" + System.currentTimeMillis());
                        myAgent.send(message);

                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));

                        //Just in Case
                        message = null;

                        step++;
                    }
                    else {
                        System.out.println("No Agents Found");
                        done = true;
                    }

                    break;

                case 1:
                    System.out.println("Step 1");
                    message = myAgent.receive(mt);
                    if(message != null) {

                        if(message.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(message.getSender())) {
                                    agent.setCapacity(Integer.parseInt(message.getContent()));
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + message.getSender().toString() + " supplied an invalid capacity");
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }

                        replyCount++;
                        System.out.println(getLocalName() + ": Received " + replyCount + " replies out of " + agents.size());
                        if(replyCount >= agents.size()) {

                            //Just in Case
                            message = null;

                            replyCount = 0;
                            step++;
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
                    ArrayList<Item> items = new ArrayList<>();
                    Item item1 = new Item(1, "Item1", 2, 1, 1);
                    Item item2 = new Item(2, "Item2", 5, 1, 1);
                    Item item3 = new Item(3, "Item3", 7, 1, 1);
                    Item item4 = new Item(4, "Item4", 6, 1, 1);
                    Item item5 = new Item(5, "Item5", 1, 1, 1);
                    Item item6 = new Item(6, "Item6", 9, 1, 1);
                    Item item7 = new Item(7, "Item7", 3, 1, 1);
                    Item item8 = new Item(8, "Item8", 3, 1, 1);
                    Item item9 = new Item(9, "Item9", 6, 1, 1);

                    items.add(item1);
                    items.add(item2);
                    items.add(item3);

                    Inventory i1 = new Inventory(items);

                    items.clear();
                    items.add(item4);
                    items.add(item5);
                    items.add(item6);
                    Inventory i2 = new Inventory(items);

                    items.clear();
                    items.add(item7);
                    items.add(item8);
                    items.add(item9);
                    Inventory i3 = new Inventory(items);


                    //TODO: CREATE DUMMY PATH DATA
                    Path p1 = new Path(new int[]{1,3,5,4,7}, new int[]{4,3,2,5,2});
                    Path p2 = new Path(new int[]{6,1,2,9}, new int[]{2,5,3,1});
                    Path p3 = new Path(new int[]{2,3,6}, new int[]{5,3,6});

                    //Add Inventories and Paths to the AgentData Objects
                    try{
                        agents.get(0).setJsonInventory(i1.serialize());
                        agents.get(0).setJsonPath(p1.serialize());

                        agents.get(1).setJsonInventory(i2.serialize());
                        agents.get(1).setJsonPath(p2.serialize());

                        agents.get(2).setJsonInventory(i3.serialize());
                        agents.get(2).setJsonPath(p3.serialize());
                    }catch(Exception ex) {
                        System.out.println("We're Idiots");
                        ex.printStackTrace();
                    }

                    //Set up Message
                    message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId("processRoute");
                    message.setReplyWith(Message.INVENTORY + System.currentTimeMillis());
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));

                    //Send Inventory to Each Agent
                    for(AgentData agent: agents) {
                        if(!agent.getJsonInventory().isEmpty()) {
                            message.addReceiver(agent.getName());
                            message.setContent(Message.INVENTORY + ":" + agent.getJsonInventory());
                            myAgent.send(message);
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + agent.getName().toString() + " has an empty json inventory string");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    //Just in Case
                    message = null;

                    step++;

                    break;

                case 3:
                    System.out.println("Step 3");
                    //Process Inventory Replies
                    message = myAgent.receive(mt);
                    if(message != null) {

                        if(message.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(message.getSender())) {
                                    //Set the inventory in AgentData Object here, but we don't need to do anything in here for now.
                                }
                            }
                            if(message.getContent().equals(Message.INVENTORY_SUCCESS)) {
                                //Do Nothing, this is what we want
                            }
                            else if(message.getContent().equals(Message.INVENTORY_FAILURE)) {
                                try {
                                    throw new Exception(myAgent.getLocalName() + ": ERROR - " + message.getSender().toString() + " could not load supplied inventory");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + message.getSender().toString() + " replied with incorrect performative");
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }

                        replyCount++;
                        if(replyCount >= agents.size()) {

                            //Just in Case
                            message = null;

                            replyCount = 0;
                            step++;
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
                    message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId("processRoute");
                    message.setReplyWith(Message.PATH + System.currentTimeMillis());
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));

                    //Send Inventory to Each Agent
                    for(AgentData agent: agents) {
                        if(!agent.getJsonInventory().isEmpty()) {
                            message.addReceiver(agent.getName());
                            message.setContent(Message.PATH + ":" + agent.getJsonPath());
                            myAgent.send(message);
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + agent.getName().toString() + " has an empty json path string");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    //Just in Case
                    message = null;

                    step++;
                    break;

                case 5:
                    System.out.println("Step 5");
                    //Process all replies
                    message = myAgent.receive(mt);
                    if(message != null) {

                        if(message.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(message.getSender())) {
                                    //Set the path in AgentData Object here, but we don't need to do anything in here for now.
                                }
                            }
                            if(message.getContent().equals(Message.PATH_SUCCESS)) {
                                //Do Nothing, this is what we want
                            }
                            else if(message.getContent().equals(Message.PATH_FAILURE)) {
                                try {
                                    throw new Exception(myAgent.getLocalName() + ": ERROR - " + message.getSender().toString() + " could not load supplied path");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else {
                            try {
                                throw new Exception(myAgent.getLocalName() + ": ERROR - " + message.getSender().toString() + " replied with incorrect performative");
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }

                        replyCount++;
                        if(replyCount >= agents.size()) {

                            //Just in Case
                            message = null;

                            replyCount = 0;
                            step++;
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
                    message = new ACLMessage(ACLMessage.REQUEST);
                    for(AgentData agent: agents) {
                        message.addReceiver(agent.getName());
                    }
                    message.setContent(Message.START);
                    myAgent.send(message);

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
}
