package Agent.AgentInfo;

import Item.Inventory;
import jade.core.AID;

//Data Holding Object
//The Master Router has an array of these, one for each DA it is aware of
//In the Master Routing Agents ProcessRoutes and ListenForMessages behaviours,
//each time the DA delivers an item, moves to a location etc, the Master Routing Agent will update this object.

public class AgentData {
    //The Agent ID (from the Jade library), that refers to this object. This can be used as the receiver in a jade message
    private AID name;

    //The delivery agents capacity, this gets updated by the Master Router in step 1 of the ProcessRoutes behaviour
    private int capacity;

    //The delivery agents current location, this gets updated by the Master Router in the ListenForMessages behaviour
    private int currentLocation;

    //Inventory of items the delivery agent is currently holding.
    //Gets added to in step 3 of the Master Routers Process Routes Behaviour, upon receiving a success message from the Delivery Agent
    //Items are removed each time the DA sends an item delivered message, which is processed in the ListenForMessages behaviour
    public Inventory inventory;


    //These two fields are used in the Process Routes behaviour, but have no use afterwards.
    //Json Representation of the Inventory that is to be sent to the DA
    private String jsonInventory;

    //Json Representation of the Path that is to be sent to the DA
    private String jsonPath;

    //Constructor
    //This is used as soon as the Master Router finds a DA with the AMS Service.
    //At this point, the Master Routers and Delivery Agents have not messaged each other,
    //so this constructor only accepts the AID (received from the AMS Service)
    public AgentData(AID a) {
        name = a;
        capacity = 0;
        currentLocation = 0;
        inventory = new Inventory();
        jsonInventory = "";
        jsonPath = "";
    }

    //Simple way of matching this data with an AID, if iterating through a list of these objects
    public boolean matchData(AID a) {
        //TODO: This Might Not Be The Best Solution, And May Create Errors With A Multi-Container System
        return name.getLocalName().equals(a.getLocalName());
    }

    //Field Getters and Setters
    public AID getName() {
        return name;
    }

    public void setName(AID name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(int currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getJsonInventory() {
        return jsonInventory;
    }

    public void setJsonInventory(String jsonInventory) {
        this.jsonInventory = jsonInventory;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }
}