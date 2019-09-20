package Item;

import com.google.gson.Gson;

//TODO: Probably fix an edge case where deserialize potentially creates a weird object if json represenation is invalid

public class Item {
    //Fields
    //int id - Unique ID for this particlar item
    //String name - Name for the item, to be used in output, rather than ID
    //int destination - ID of destination location node for this item
    //int weight - Used to calculate if the Delivery Agent holding this item is at capacity or not
    //int size - Same as weight
    private int id;
    private String name;
    private int destination;
    private int weight;
    private int size;

    //Regular Constructor
    public Item(int id, String name, int destination, int weight, int size) {
        this.id = id;
        this.name = name;
        this.destination = destination;
        this.weight = weight;
        this.size = size;
    }

    //Return JSON representation of this item
    public String serialize() {
        Gson gson = new Gson();
        String json = gson.toJson(this);

        return json;
    }

    //Static Method, used to create Items from a JSON representation
    public static Item deserialize(String JSON) {
        Gson gson = new Gson();
        Item i = gson.fromJson(JSON, Item.class);
        return i;
    }

    //Returns Item fields in an easily readable format
    @Override
    public String toString() {
        return "Item " + id + ": " + name + ", Going To: " + destination + ". Weight: " + weight + ", Size: " + size;
    }

    //Field Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    public int getSize() {
        return size;
    }
}
