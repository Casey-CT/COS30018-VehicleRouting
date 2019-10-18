package Agent;

import Communication.Message;
import DeliveryPath.Path;
import GUI.DeliveryAgentInterface;
import Item.Inventory;
import Item.Item;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.mobility.BehaviourLoadingVocabulary;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class DeliveryAgent extends Agent implements DeliveryAgentInterface {
    private int capacity = 0;
	private int currentLocation = 0;
	private Inventory inventory = new Inventory();
	private Path path;
	private AID MRA_ID;
	
    public int getCapacity() {
        return capacity;
    }

    public int getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public String getData() {
        String s = "asdfjadf;klajsdf;l";


        return s;
    }

    protected void setup() {
        System.out.println(getAID().getName() + ": I have been created");
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            capacity = Integer.parseInt(args[0].toString());
            System.out.println(getAID().getName() + ": My capacity is: " + capacity);
            addBehaviour(new ListenForMessages());
            //Traversal and Delivery Test Data
        } else {
            // Make the agent terminate immediately
            System.out.println("No capacity specified!");
            doDelete();
        }
    }

    //Used to restart the agent if paused
    protected void start() {
        addBehaviour(new Travel());
    }

    //Used to pause the agent and all it's behaviours
    protected void pause() {
    }

    //Reusable function for sending a message to master agent
    protected void messageMaster(int performative, String content) {

    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Delivery agent " + getAID().getName() + " terminating.");
        doDelete();
    }

    private class ListenForMessages extends CyclicBehaviour {
        public void action() {
            //MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            //Create a message template for our purposes
				
			//check if message performative is REQUEST and content is "KILL"
			//kill the DA 
			//run agent ShutdownAgent behaviour
			//myAgent.takeDown();

            ACLMessage msg = myAgent.receive();       //pass mt to receive()

            if (msg != null) {
                System.out.println(myAgent.getLocalName() + ": Message Received");
                //Message received. Process it.
                //MessageReader mr = new MessageReader();
                //Items[] = mr.Read(msg.getContent());            //assuming we have an Items class

                String messageContent = msg.getContent();
                MRA_ID = msg.getSender();

                if(msg.getPerformative() == ACLMessage.INFORM) {
                    String[] jsonMessage = messageContent.split(":", 2);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);

                    if(jsonMessage[0].equals(Inventory.INVENTORY)) {
                        if(loadInventory(jsonMessage[1])) {
                            reply.setContent(Message.INVENTORY_SUCCESS);
                            send(reply);
                            System.out.println(myAgent.getLocalName() + ": Sending Inventory Success Message");
                        } else {
                            reply.setContent(Message.INVENTORY_FAILURE);
                            send(reply);
                            System.out.println(myAgent.getLocalName() + ": Sending Inventory Failure Message");
                        }
                    }

                    else if(jsonMessage[0].equals(Path.PATH)) {
                        if(loadPath(jsonMessage[1])) {
                            reply.setContent(Message.PATH_SUCCESS);
                            send(reply);
                            System.out.println(myAgent.getLocalName() + ": Sending Path Success Message");
                        } else {
                            reply.setContent(Message.PATH_FAILURE);
                            send(reply);
                            System.out.println(myAgent.getLocalName() + ": Sending Path Failure Message");
                        }
                    }
                    else
                        throw new IllegalArgumentException("Wrong message type");
                } else if(msg.getPerformative() == ACLMessage.REQUEST) {
                    if(messageContent.equals(Message.CAPACITY)) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(Integer.toString(capacity));
                        send(reply);
                        System.out.println(myAgent.getLocalName() + ": Sending Capacity Message");
                    }

                    else if(messageContent.equals(Message.START))
                        start();

                    else
                        throw new IllegalArgumentException("Wrong message content");
                }
                //ACLMessage reply = msg.createReply();

                //if(at least one package has been received && package can be carried (not overloaded)) {
                //  reply.setPerformative(ACLMessage.INFORM);
                //  reply.setContent("package received");
                // }

                //else(no packages received, or package weight will overload the DA capacity)
                //  reply.setPerformative(ACLMessage.REFUSE);
                //  reply.setContent("no packages received" || "cannot carry package, over capacity, dropping package");
            }
        }
    }

    //Should be added in the message handling behaviour, when the master agent supplies an inventory
    //Accepts the JSON representation of the inventory provided by the master routing agent
    //Adds the supplied inventory to this agent's inventory
    //Compares the given inventory against this Delivery Agents Capacity and Size Limits
    private boolean loadInventory(String json)  {
            Inventory temp = Inventory.deserialize(json);
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            if(!temp.isEmpty()){
                if(!((inventory.getTotalSize() + temp.getTotalSize()) > getCapacity())) {
                    if(inventory.addInventory(temp)){
                        System.out.println(getLocalName() + ": Items Were Added.\n" + inventory.listItems());
                        reply.setContent(Message.INVENTORY_SUCCESS);
                        send(reply);
                        //TODO: Add Message to Master Agent
                        return true;
                    }
                    else {
                        System.out.println(getLocalName() + ": No Items Were Added.");
                        reply.setContent(Message.INVENTORY_FAILURE);
                        send(reply);
                        //TODO: Add Message to Master Agent
                        return false;
                    }
                }
                else {
                    System.out.println(getLocalName() + ": Supplied Inventory Exceeded Capacity.");
                    reply.setContent(Message.INVENTORY_FAILURE);
                    send(reply);
                    //TODO: Add Message to Master Agent
                    return false;
                }
            }
            else {
                System.out.println(getLocalName() + ": Supplied Inventory was Empty.");
                reply.setContent(Message.INVENTORY_FAILURE);
                send(reply);
                //TODO: Add Message to Master Agent
                return false;
            }
        }

    //Should be added in the message handling behaviour
    //Accepts the JSON representation of the path provided by master router
    //Adds the supplied path to this Delivery Agent
    private boolean loadPath(String json) {
            Path p = Path.deserialize(json);
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            if(p.isPathValid()) {
                path = p;
                System.out.println(getLocalName() + ": Path Set");
                reply.setContent(Message.PATH_SUCCESS);
                send(reply);
                return true;
            }
            else{
                System.out.println(getLocalName() + ": Supplied Path was Invalid");
                reply.setContent(Message.PATH_FAILURE);
                send(reply);
                return false;
            }
        }

    //Called when wakerbehaviour added by travel completes
    //Looks through package list, finds matching packages
    //Removes each package from list and messages master router
    //Adds a new travel behaviour
    private class onArrival extends OneShotBehaviour {
        public void action() {
            //Find Packages That Match
            //Doing a separate list of matches first, as I'm pretty sure removing elements while iterating through
            //an entire list, will cause it to explode
            ArrayList<Integer> item_match = new ArrayList<>();
            for(Item i: inventory.getItems()) {
                //Add in additional conditions here if attempting extentions
                //Checking time window, etc.
                if(i.getDestination() == getCurrentLocation()) {
                    item_match.add(i.getId());
                }
            }

            System.out.println(myAgent.getLocalName() + ": " + item_match.size() + " Items to Deliver to Location " + getCurrentLocation());

            //If there are packages to deliver
            if(!item_match.isEmpty()) {
                for(int i: item_match) {
                    System.out.println(myAgent.getLocalName() + ": Delivering Item " + i + " at Location " + getCurrentLocation());
                    if(inventory.removeItem(i)) {
                        System.out.println(myAgent.getLocalName() + ": Item " + i + " Delivered at Location " + getCurrentLocation());
                        //TODO: Add Message to Master Router
                    }
                    else {
                        System.out.println(myAgent.getLocalName() + ": ERROR - Item " + i + " Not Delivered at Location " + getCurrentLocation());
                        //TODO: Add Message to Master Router, probably error handle too.
                    }
                }
            }

            addBehaviour(new Travel());
        }
    }

    //Called By Delivery Agent When Told To Start
    //Called By onArrival to move to next location
    //-Gets Next Location
    //-Gets Next Distance
    //-Checks if path has ended
    //-Starts a new Wakeup behaviour, using the distance as a time
    //  -On Wakeup, Overwrites new location
    //  -then adds a new arrive behaviour
    private class Travel extends OneShotBehaviour {
        public void action() {
            if(!path.isPathStarted()) {
                System.out.println(myAgent.getLocalName() + ": BEGINNING PATH");
            }
            path.traversePath();
            if(!path.isPathComplete()) {
                int l = path.getNextLocation();
                int d = path.getNextDistance();
                addBehaviour(new WakerBehaviour(myAgent, d * 1000) {
                    protected void onWake() {
                        currentLocation = l;
                        myAgent.addBehaviour(new onArrival());
                        System.out.println(myAgent.getLocalName() + ": Arrived At " + l);
                    }
                });
                System.out.println(myAgent.getLocalName() + ": Travelling to " + l);
            }
            else {
                //TODO: Message Master Agent
                System.out.println(myAgent.getLocalName() + ": PATH COMPLETE");
                takeDown();
            }
        }
    }
}








