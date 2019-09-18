package Agent;

import DeliveryPath.Path;
import Item.Inventory;
import Item.Item;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class DeliveryAgent extends Agent {
    private int capacity = 0;
	private int currentLocation = 0;
	private Inventory inventory = new Inventory();
	private Path path;
	
    public int getCapacity() {
        return capacity;
    }

    public int getCurrentLocation() {
        return currentLocation;
    }

    protected void setup() {
        System.out.println(getAID().getName() + ": I have been created");
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            capacity = Integer.parseInt(args[0].toString());
            System.out.println(getAID().getName() + ": My capacity is: " + capacity);

            //Traversal and Delivery Test Data
            Inventory i = new Inventory();
            i.addItem(new Item(1, "Item 1", 2, 4, 2));
            i.addItem(new Item(2, "Item 2", 2, 5, 1));
            i.addItem(new Item(3, "Item 3", 5, 2, 1));
            i.addItem(new Item(4, "Item 4", 6, 4, 4));

            Path p = new Path(new int[]{3, 2, 4, 5, 6}, new int[]{5, 2, 4, 9, 3});

            SequentialBehaviour sq = new SequentialBehaviour();
            sq.addSubBehaviour(new loadInventory(i.serialize()));
            sq.addSubBehaviour(new loadPath(p.serialize()));
            sq.addSubBehaviour(new Travel());

            addBehaviour(sq);

        } else {
            // Make the agent terminate immediately
            System.out.println("No capacity specified!");
            doDelete();
        }
    }

    //Used to restart the agent if paused
    protected void start() {}

    //Used to pause the agent and all it's behaviours
    protected void pause() {}

    //Reusable function for sending a message to master agent
    protected void messageMaster(int performative, String content) {

    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Delivery agent " + getAID().getName() + " terminating.");
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
                //Message received. Process it.
                //MessageReader mr = new MessageReader();
                //Items[] = mr.Read(msg.getContent());            //assuming we have an Items class

                ACLMessage reply = msg.createReply();

                //if(at least one package has been received && package can be carried (not overloaded)) {
                //  reply.setPerformative(ACLMessage.INFORM);
                //  reply.setContent("package received");
                // }

                //else(no packages received, or package weight will overload the DA capacity)
                //  reply.setPerformative(ACLMessage.REFUSE);
                //  reply.setContent("no packages received" || "cannot carry package, over capacity, dropping package");
            }
            else{
                    block();
                }
            }
        }

    //Should be added in the message handling behaviour, when the master agent supplies an inventory
    //Accepts the JSON representation of the inventory provided by the master routing agent
    //Adds the supplied inventory to this agent's inventory
    //Compares the given inventory against this Delivery Agents Capacity and Size Limits
    private class loadInventory extends OneShotBehaviour {

        String json;

        public loadInventory(String json) {
            this.json = json;
        }

        public void action() {
            Inventory temp = Inventory.deserialize(json);
            if(!temp.isEmpty()){
                if(!((inventory.getTotalSize() + temp.getTotalSize()) > getCapacity())) {
                    if(inventory.addInventory(temp)){
                        System.out.println(myAgent.getLocalName() + ": Items Were Added.\n" + inventory.listItems());
                        //TODO: Add Message to Master Agent
                    }
                    else {
                        System.out.println(myAgent.getLocalName() + ": No Items Were Added.");
                        //TODO: Add Message to Master Agent
                    }
                }
                else {
                    System.out.println(myAgent.getLocalName() + ": Supplied Inventory Exceeded Capacity.");
                    //TODO: Add Message to Master Agent
                }
            }
            else {
                System.out.println(myAgent.getLocalName() + ": Supplied Inventory was Empty.");
                //TODO: Add Message to Master Agent
            }
        }
    }

    //Should be added in the message handling behaviour
    //Accepts the JSON representation of the path provided by master router
    //Adds the supplied path to this Delivery Agent
    private class loadPath extends OneShotBehaviour {

        String json;

        public loadPath(String json) {
            this.json = json;
        }

        public void action() {
            Path p = Path.deserialize(json);
            if(p.isPathValid()) {
                path = p;
                System.out.println(myAgent.getLocalName() + ": Path Set");
            }
            else{
                System.out.println(myAgent.getLocalName() + ": Supplied Path was Invalid");
            }
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
            }

        }
    }
}








