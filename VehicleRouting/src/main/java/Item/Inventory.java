package Item;

import com.google.gson.Gson;

import java.util.ArrayList;

//TODO: Some of the list manipulation methods could probably be more efficient, and loop through the list less

//Inventory class to be used by both Master Agent and Delivery Agents
//Master Agent will serialize one of these objects for each delivery agent, when it creates a route and assigns packages
public class Inventory {
    private ArrayList<Item> items;

    //Constructor. For creating an inventory with items
    public Inventory(ArrayList<Item> items) {
        this.items = items;

        if (!isListValid()) {
            throw new IllegalArgumentException("ERROR: Each Item in List Must Have a Unique ID");
        }
    }

    //Constructor. For Creating an Empty Inventory
    public Inventory() {
        items = new ArrayList<Item>();
    }

    //Serialization Methods
    //Returns JSON representation of this Inventory Object
    public String serialize() {
        Gson gson = new Gson();
        String json = gson.toJson(this);

        return json;
    }

    //Static Method, which returns a new Inventory Object, created from a JSON representation
    //If Inventory is invalid, either from JSON or Invalid ids, returns an empty inventory
    public static Inventory deserialize(String json) {
        Gson gson = new Gson();
        Inventory i;
        try {
            i = gson.fromJson(json, Inventory.class);

            if(!i.isListValid()) {
                throw new IllegalArgumentException("Item List Created From JSON Representation Does Not Have Unique Item IDs");
            }
        } catch(Exception ex) {
            return new Inventory();
        }

        return i;
    }

    //TODO: Add Wrapper methods so Inventories can be iterated directly, instead of using the getItems() method
    //Underlying List Manipulation Methods
    //Returns the number of Items in this Inventory
    public int getLength() {
        return items.size();
    }

    //Checks if any item in this inventory has a matching id
    public boolean hasItem(int id) {
        for (Item i: items) {
            if(i.getId() == id) {
                return true;
            }
        }
        return false;
    }

    //Returns first item with matching id, otherwise returns null
    public Item getItem(int id) {
        if(hasItem(id)) {
            for (Item i: items) {
                if(i.getId() == id) {
                    return i;
                }
            }
        }
        return null;
    }

    //Adds an item into this inventory
    //If an item is added successfully, return true
    public boolean addItem(Item item) {
        if(!hasItem(item.getId())) {
            return items.add(item);
        }
        else return false;
    }

    //TODO: Fix up this method.
    // Ideally, if every item in the supplied inventory are not added, this function should return false, or throw an exception
    // Adding only half the supplied inventory, and still returning true isn't an ideal scenario
    //Adds all Items in Parameter Inventory to this Inventory
    //As it uses the addItem function, it will only add items with ids that do not currently exist in this inventory
    //Return true if items added, false otherwise
    public boolean addInventory(Inventory inventory) {
        if(!inventory.isEmpty()) {
            for(Item i: inventory.getItems()) {
                addItem(i);
            }
            return true;
        }
        else return false;
    }

    //Removes first found instance of item with matching id
    //If an item is removed, return true
    public boolean removeItem(int id) {
        if(hasItem(id)) {
            for(Item i: items) {
                if (i.getId() == id) {
                    return items.remove(i);
                }
            }
        }
        return false;
    }

    //Returns true is items is empty
    public boolean isEmpty() {
        return items.isEmpty();
    }

    //Item Count Methods
    //Returns total weight of all items in items arraylist
    public int getTotalWeight() {
        int result = 0;

        if(!isEmpty()) {
            for (Item i: items) {
                result += i.getWeight();
            }
        }

        return result;
    }

    //Returns total size of all items in items arraylist
    public int getTotalSize() {
        int result = 0;

        if(!isEmpty()) {
            for (Item i: items) {
                result += i.getSize();
            }
        }

        return result;
    }

    //Returns a String List of Item Details
    public String listItems() {
        String result = "";

        if(!isEmpty()) {
            result += "Currently Carrying:\n";
            for(int i = 0; i < items.size(); i++) {
                if(i == items.size() - 1){
                    result += items.get(i).toString();
                }
                else result += items.get(i).toString() + "\n";
            }
        }
        else return "Inventory is Empty!";

        return result;
    }

    //List Validator
    //Makes sure each id in the item arraylist is unique
    private boolean isListValid() {
        for (int i = 0; i < items.size() - 1; i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if (items.get(i).getId() == items.get(j).getId()) {
                    return false;
                }
            }
        }
        return true;
    }

    //Returns underlying ArrayList of Items
    public ArrayList<Item> getItems() {
        return items;
    }
}