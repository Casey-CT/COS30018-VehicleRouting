package Agent;

import Agent.AgentInfo.AgentData;
import DeliveryPath.Path;
import Item.Inventory;
import com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class MasterRoutingAgent {
    public static final String STOP = "STOP";
    public static final String START = "START";
    public static final String CAPACITY = "CAPACITY";

    private ArrayList<AgentData> agents = new ArrayList<>();

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

        //Is Behvaiour Done?
        private boolean done = false;

        public void action() {
            switch(step) {
                case 0:
                    //Find all Delivery Agents with AMS
                    //Send them a request for their information
                    AMSAgentDescription[] a = null;

                    try {
                        SearchConstraints c = new SearchConstraints();
                        c.setMaxDepth((long) -1);
                        a = AMSService.search(myAgent, new AMSAgentDescription(), c);
                    } catch (Exception ex) {
                        System.out.println(myAgent.getLocalName() + ": ERROR in Finding Delivery Agents" + ex );
                        ex.printStackTrace();
                    }

                    for(int i = 0; i < a.length; i++) {
                        if(a[i].getName().toString().contains("DeliveryAgent")) {
                            agents.add(new AgentData(a[i].getName()));
                        }
                    }

                    if(agents.size() > 0) {
                        message = new ACLMessage(ACLMessage.REQUEST);
                        for(AgentData agent: agents) {
                            message.addReceiver(agent.getName());
                        }
                        message.setContent(MasterRoutingAgent.CAPACITY);
                        message.setConversationId("processRoute");
                        message.setReplyWith("Request" + System.currentTimeMillis());
                        myAgent.send(message);

                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));

                        //Just in Case
                        message = null;

                        step++;
                    }
                    else {done = true;}

                    break;

                case 1:
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
                        if(replyCount >= agents.size()) {

                            //Just in Case
                            message = null;

                            replyCount = 0;
                            step++;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                case 2:
                    //TODO: CREATE DUMMY INVENTORY DATA
                    //TODO: CREATE DUMMY PATH DATA
                    //Add Inventories and Paths to the AgentData Objects
                    for(AgentData agent: agents) {
                        //TODO: Add Data to each object
                    }

                    //Set up Message
                    message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId("processRoute");
                    message.setReplyWith("Inventory" + System.currentTimeMillis());
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));

                    //Send Inventory to Each Agent
                    for(AgentData agent: agents) {
                        if(!agent.getJsonInventory().isEmpty()) {
                            message.addReceiver(agent.getName());
                            message.setContent("INVENTORY:" + agent.getJsonInventory());
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
                    //Process Inventory Replies
                    message = myAgent.receive(mt);
                    if(message != null) {

                        if(message.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(message.getSender())) {
                                    //Set the inventory in AgentData Object here, but we don't need to do anything in here for now.
                                }
                            }
                            if(message.getContent().equals("INVENTORY_SUCCESS")) {
                                //Do Nothing, this is what we want
                            }
                            else if(message.getContent().equals("INVENTORY_FAILURE")) {
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
                    }
                    break;

                case 4:
                    //Send Paths to all DAs
                    //Set up Message
                    message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId("processRoute");
                    message.setReplyWith("Path" + System.currentTimeMillis());
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("processRoute"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));

                    //Send Inventory to Each Agent
                    for(AgentData agent: agents) {
                        if(!agent.getJsonInventory().isEmpty()) {
                            message.addReceiver(agent.getName());
                            message.setContent("Path:" + agent.getJsonPath());
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
                    //Process all replies
                    message = myAgent.receive(mt);
                    if(message != null) {

                        if(message.getPerformative() == ACLMessage.INFORM) {
                            for (AgentData agent: agents) {
                                if(agent.matchData(message.getSender())) {
                                    //Set the path in AgentData Object here, but we don't need to do anything in here for now.
                                }
                            }
                            if(message.getContent().equals("PATH_SUCCESS")) {
                                //Do Nothing, this is what we want
                            }
                            else if(message.getContent().equals("PATH_FAILURE")) {
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
                    }
                    break;

                case 6:
                    //Tell all DAs to Start
                    message = new ACLMessage(ACLMessage.REQUEST);
                    for(AgentData agent: agents) {
                        message.addReceiver(agent.getName());
                    }
                    message.setContent("START");
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
