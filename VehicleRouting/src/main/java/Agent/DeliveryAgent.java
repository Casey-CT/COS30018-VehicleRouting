package Agent;

import Communication.Message;
import DeliveryPath.Path;
import GUI.DeliveryAgentInterface;
import Item.Inventory;
import Item.Item;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public class DeliveryAgent extends Agent implements DeliveryAgentInterface {
    //This agents capacity (compared against the weight of the items in its inventory)
    //Has a default value of 0, so if capacity is not specified when an agent of this type is spun up, it can be terminated easily
    private int capacity = 0;

    //Current Node ID of the location this delivery agent is currently at
    //TODO: Decide if map begins indexing at 0 or 1, and update this default value
	private int currentLocation = 0;

	//Inventory to be used by this object
	private Inventory inventory = new Inventory();

	//Path to be used by this object
	private Path path;

	//Saved copy of the Master Routing Agents Jade ID, to be used by the messageMaster() function
	private AID MRA_ID;

	//Boolean used as a flag to signal if DA is returning to depot
    private boolean returning = false;

	//Returns this agents capacity
    public int getCapacity() {
        return capacity;
    }

    //Returns the node ID of this agents current location
    public int getCurrentLocation() {
        return currentLocation;
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

        registerO2AInterface(DeliveryAgentInterface.class, this);
    }

    //Called by the ListenForMessages behaviour
    protected void start() {
        addBehaviour(new Travel());
    }

    //Used to pause the agent and all it's behaviours
    //TODO: Actually Create This Method
    protected void pause() {
    }

    //Reusable function for sending a message to master agent
    //
    //Parameters
    // performative: An ACLMessage constant, which is of type int
    // content: the content to be applied to the message
    protected void messageMaster(int performative, String content) {
        if(MRA_ID != null) {
            ACLMessage message = new ACLMessage(performative);
            message.addReceiver(MRA_ID);
            message.setContent(content);
            send(message);
            Message.outputMessage(message);
        }
        else {
            try{
                throw new Exception(getLocalName() + ": MRA_ID has not yet been allocated");
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Delivery agent " + getAID().getName() + " terminating.");
        doDelete();
    }

    //Constantly Loops, and checks for messages
    //If a message is found, it reviews the content, and acts accordingly
    //
    //Message Content format; IDENTIFIER:data
    //IDENTIFIER, is any of the fields in the Message class (Communication.Message)
    //data is the data that is processed, depending on the IDENTIFIER, this is usually a JSON representation
    //First the performative of the message is viewed,
    //Then the message content is passed to the appropriate function
    //
    //Depending on the performative, the message content is then split, using the delimiter character ":"
    //The first substring determines the type of data in the message content
    //The second is the data, usually JSON representation of objects
    private class ListenForMessages extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                //Message received. Process it.
                System.out.println(myAgent.getLocalName() + ": Message Received");

                String messageContent = msg.getContent();

                if(msg.getPerformative() == ACLMessage.INFORM) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);

                    //Received New Inventory
                    //Message Format Message.INVENTORY:json Inventory
                    if(messageContent.contains(Message.INVENTORY)) {
                        try {
                            String[] jsonMessage = messageContent.split(":", 2);
                            System.out.println(myAgent.getLocalName() + ": Received New Inventory Message");

                            if(loadInventory(jsonMessage[1])) {
                                reply.setContent(Message.INVENTORY_SUCCESS);
                                send(reply);
                                System.out.println(myAgent.getLocalName() + ": Sending Inventory Success Message");
                            } else {
                                reply.setContent(Message.INVENTORY_FAILURE);
                                send(reply);
                                System.out.println(myAgent.getLocalName() + ": Sending Inventory Failure Message");
                            }

                            Message.outputMessage(reply);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println(myAgent.getLocalName() + ": Caused Exception While Processing Inventory Message");
                        }
                    }

                    //Received New Path
                    //Message Format Message.INVENTORY:json Path
                    else if(messageContent.contains(Message.PATH)) {
                        try {
                            String[] jsonMessage = messageContent.split(":", 2);
                            System.out.println(myAgent.getLocalName() + ": Received New Path Message");

                            if(loadPath(jsonMessage[1])) {
                                reply.setContent(Message.PATH_SUCCESS);
                                send(reply);
                                System.out.println(myAgent.getLocalName() + ": Sending Path Success Message");
                            } else {
                                reply.setContent(Message.PATH_FAILURE);
                                send(reply);
                                System.out.println(myAgent.getLocalName() + ": Sending Path Failure Message");
                            }

                            Message.outputMessage(reply);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println(myAgent.getLocalName() + ": Caused Exception While Processing Path Message");
                        }
                    }

                    else
                        try {
                            throw new IllegalArgumentException(myAgent.getLocalName() + ": Received Wrong message type");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                }

                else if(msg.getPerformative() == ACLMessage.REQUEST) {
                    //Status Message, Requesting currentLocation and capacity
                    if(messageContent.equals(Message.STATUS)) {
                        System.out.println(myAgent.getLocalName() + ": Received Status Request");

                        //If MRA_ID has not been set yet, set it
                        //A request message should be the first message this agent receives, so this should be a safe solution
                        //secondly, only the MRA can send a message of this type
                        if(MRA_ID == null) {
                            MRA_ID = msg.getSender();
                        }

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);

                        //Message Format capacity;currentLocation
                        reply.setContent(capacity + "," + currentLocation);
                        send(reply);
                        Message.outputMessage(reply);
                        System.out.println(myAgent.getLocalName() + ": Sending Status Message");
                    }

                    else if(messageContent.equals(Message.START)) {
                        System.out.println(myAgent.getLocalName() + ": Received Start Request");
                        start();
                    }

                    //Should receive a message of this type when agent has completed deliveries
                    //Provides a new path leading back to node 0 (Depot)
                    else if(messageContent.contains(Message.RETURN)) {
                        try {
                            //Message Format Message.RETURN:json Path
                            String[] jsonMessage = messageContent.split(":", 2);
                            if(loadPath(jsonMessage[1])) {
                                System.out.println(myAgent.getLocalName() + ": Added Return Path.");
                                returning = true;
                                addBehaviour(new Travel());
                            }
                            else {
                                messageMaster(ACLMessage.FAILURE, Message.ERROR);
                                System.out.println(myAgent.getLocalName() + ": Error in Processing Supplied Return Path");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println(myAgent.getLocalName() + ": Caused Exception While Processing Return Message");
                        }
                    }

                    else
                        try {
                            throw new IllegalArgumentException("Wrong message content");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                }
            }
        }
    }

    //Called in the ListenForMessages behaviour, when the master agent supplies an inventory
    //
    //Parameters
    // json: String representation of an Inventory Object
    //
    //Deserializes an Inventory from the json paramater
    //Compares the given inventory against this Delivery Agents capacity, and Whether it is a valid inventory
    //If it is, it attempts to add the supplied inventory to its own inventory object (using the addInventory() method from the Inventory class)
    //returns the result of inventory.addIntentory(), using the deserialized json inventory
    private boolean loadInventory(String json)  {
        Inventory temp = Inventory.deserialize(json);
        if(!temp.isEmpty()){
            if(!((inventory.getTotalSize() + temp.getTotalSize()) > getCapacity())) {
                if(inventory.addInventory(temp)){
                    System.out.println(getLocalName() + ": Items Were Added.\n" + inventory.listItems());
                    return true;
                }
                else {
                    System.out.println(getLocalName() + ": No Items Were Added.");
                    return false;
                }
            }
            else {
                System.out.println(getLocalName() + ": Supplied Inventory Exceeded Capacity.");
                return false;
            }
        }
        else {
            System.out.println(getLocalName() + ": Supplied Inventory was Empty.");
            return false;
        }
    }

    //Called in the ListenForMessages behaviour, when the master agent supplied a path
    //
    //Parameters
    //json: JSON representation of a Path object
    //
    //Deserializes the supplied Path object from the json parameter
    //Checks if it is valid, using the isPathValid() method, from the Path class
    //If valid, Adds the supplied path to this Delivery Agent
    //Sends either success of failure to the Master Agents
    private boolean loadPath(String json) {
        Path p = Path.deserialize(json);
        if(p.isPathValid()) {
            path = p;
            System.out.println(getLocalName() + ": Path Set");
            return true;
        }
        else{
            System.out.println(getLocalName() + ": Supplied Path was Invalid");
            return false;
        }
    }

    //Added by WakerBehaviours added by the Travel behaviour
    //
    //Loops through inventory, and copies ids of items that match
    //If the list of item matches is not empty, it is iterated through, and any matching packages are removed from the inventory
    //Sends a message to Master Agent, based on the returned boolean value of inventory.removeItem();
    //
    //Once iteration is complete, this behaviour adds a new Travel behaviour
    private class onArrival extends OneShotBehaviour {
        public void action() {
            if(!inventory.isEmpty()) {
                //Placing found matches in a seperate list, so removing items while iterating does not cause issues
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
                            messageMaster(ACLMessage.INFORM, Message.DELIVERED + ":" + i);
                            if(inventory.isEmpty()) {
                                System.out.println(myAgent.getLocalName() + ": No Items Remaining. Returning to Depot");
                            }
                        }
                        else {
                            System.out.println(myAgent.getLocalName() + ": ERROR - Item " + i + " Not Delivered at Location " + getCurrentLocation());
                            messageMaster(ACLMessage.FAILURE, Message.ERROR);
                        }
                    }
                }
            }

            addBehaviour(new Travel());
        }
    }

    //This behaviour is added by the start() method, initially
    //While in the travel/deliver loop, this behaviour is added by the OnArrival behaviour;
    //
    //Calls the Paths Traverse Method
    //Checks if Path is Complete, using the Paths isPathComplete() method
    //
    //If Path is Not Complete:
    //-Gets the next location and distance to location, using the Paths getNextLocation() and getNextDistance() methods
    //-These two values are used to create a new WakerBehaviour, which triggers after seconds, equal to the distance to the next location
    //-When the WakerBehaviour triggers, the currentLocation variable is updated, and an OnArrival behaviour is added
    //
    //If Path is Complete:
    //-This agents Path Object is nulled
    //-A COMPLETE message is sent to the Master Router if the returning boolean is false
    //-If the returning boolean is true, it is set to false. A COMPLETE message is not sent, as a returning path is not required.
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
                        messageMaster(ACLMessage.INFORM, Message.ARRIVE + ":" + currentLocation);
                    }
                });
                System.out.println(myAgent.getLocalName() + ": Travelling to " + l);
            }
            else {
                System.out.println(myAgent.getLocalName() + ": Path Complete");
                path = null;
                if(returning) {
                    System.out.println(myAgent.getLocalName() + ": Returned to Depot");
                    returning = false;
                }
                else {
                    messageMaster(ACLMessage.INFORM, Message.COMPLETE);
                }
            }
        }
    }

    //Overwriting DeliveryAgentInterface Methods
    @Override
    //Returns formatted String, containing getLocalName(), getCurrentLocation(), inventory.getLength() and inventory.listItems()
    public String getData() {
        String result = getLocalName() + "\n" + "Currently At Node: " + getCurrentLocation()
                        + "\nCarrying " + inventory.getLength() + " items.\n" + inventory.listItems();
        return result;
    }

    //Parameters
    //OutputStream out: The new targetted OutputStream
    //
    //Sets this agent's output to the new OutputStream
    @Override
    public void OverwriteOutput(OutputStream out) {
        System.setOut(new PrintStream(out, true));
    }

    @Override
    public AID getAgentName() {
        return getAID();
    }

    @Override
    public Path getPath() {
        return path;
    }
}