package Agent.AgentInfo;

import Item.Inventory;
import jade.core.AID;

public class AgentData {
    private AID name;
    private int capacity;
    private int currentLocation;
    public Inventory inventory;
    private String jsonInventory;
    private String jsonPath;

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
