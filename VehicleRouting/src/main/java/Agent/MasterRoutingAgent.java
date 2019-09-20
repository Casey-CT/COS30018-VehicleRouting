package Agent;

import DeliveryPath.Path;
import Item.Inventory;
import Item.Item;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MasterRoutingAgent extends Agent {
    public static final String STOP = "STOP";
    public static final String START = "START";
    public static final String CAPACITY = "CAPACITY";
    public static final String PATH_SUCCESS = "PATH_SUCCESS";
    public static final String PATH_FAILURE = "PATH_FAILURE";
    public static final String INVENTORY_SUCCESS = "INVENTORY_SUCCESS";
    public static final String INVENTORY_FAILURE = "INVENTORY_FAILURE";

    private Inventory masterInventory;
    private int[][] mapData;
    private ArrayList<Path> paths = new ArrayList<>();

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Path> paths) {
        this.paths = paths;
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

    protected void setup() {
        System.out.println("Setting up: " + getAID().getName());
        initialize();
    }

    private void initialize() {
        masterInventory.addItem(new Item(1, "Item 1", 2, 4, 2));
        masterInventory.addItem(new Item(2, "Item 2", 2, 5, 1));
        masterInventory.addItem(new Item(3, "Item 3", 5, 2, 1));
        masterInventory.addItem(new Item(4, "Item 4", 6, 4, 4));

        paths.add(new Path(new int[]{3, 2, 4, 5, 6}, new int[]{5, 2, 4, 9, 3}));
    }

    private class RequestPerformer extends Behaviour {
        public void action() {
            System.out.println("Sending INVENTORY");
            ACLMessage inventoryMessage;
        }
        public boolean done() {
            return false;
        }
    }

    protected void start() {}

    protected void stop() {}
}
