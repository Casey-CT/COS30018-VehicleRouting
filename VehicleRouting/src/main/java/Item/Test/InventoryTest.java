package Item.Test;

import Item.Inventory;
import Item.Item;

import java.util.ArrayList;

public class InventoryTest {
    public static void main(String[] args) {

        ArrayList<Item> data = new ArrayList<Item>();
        Item item1 = new Item(1, "Item1", 1, 5, 6);
        Item item2 = new Item(2, "Item2", 2, 7, 9);
        Item item3 = new Item(3, "Item3", 3, 2, 4);
        Item item4 = new Item(4, "Item4", 1, 8, 8);

        data.add(item1);
        data.add(item2);
        data.add(item3);

        Inventory i = new Inventory(data);

        //Test Item Count Methods on an Empty Inventory
        System.out.println("Testing Item Count Methods on an Empty Inventory");
        Inventory j = new Inventory();
        System.out.println("List Items");
        System.out.println(j.listItems());
        System.out.println("Total Weight: " + j.getTotalWeight());
        System.out.println("Total Size: " + j.getTotalSize());

        //Test Item Count Methods on a Filled Inventory
        System.out.println();
        System.out.println("Testing Item Count Methods on a Filled List");
        System.out.println("List Items");
        System.out.println(i.listItems());
        System.out.println("Total Weight: " + i.getTotalWeight());
        System.out.println("Total Size: " + i.getTotalSize());

        //Test List Manipulation Methods
        System.out.println();
        System.out.println("Testing List Manipulation Methods");
        System.out.println("Testing Methods on Empty List");
        System.out.println("Empty Inventory Length: " + j.getLength());
        System.out.println("Is Inventory Empty: " + j.isEmpty());
        System.out.println("Checking For Item in Empty List: " + j.hasItem(1));
        System.out.println("Getting Item from Empty List: " + j.getItem(1));
        System.out.println("Removing Item from Empty List: " + j.removeItem(1));
        System.out.println("Adding Item to Empty Inventory: " + j.addItem(item4));
        System.out.println("Is Inventory Empty: " + j.isEmpty());

        System.out.println();
        System.out.println("Testing Methods on Filled List, With Invalid Data");
        System.out.println("Adding Item, with ID already in the List: " + i.addItem(item1));
        System.out.println("Removing Item not in the List: " + i.removeItem(4));
        System.out.println("Finding Item Not in the List: " + i.hasItem(4));
        System.out.println("Getting Item Not in the List: " + i.getItem(4));

        System.out.println();
        System.out.println("Testing Methods on Filled List, With Valid Data");
        System.out.println("Is List Empty: " + i.isEmpty());
        System.out.println("List Length: " + i.getLength());
        System.out.println("Adding Item: " + i.addItem(item4));
        System.out.println("Has Item: " + i.hasItem(4));
        System.out.println("Find Item: " + i.getItem(4).getId());
        System.out.println("Remove Item: " + i.removeItem(4));

        //Test Serialize and Deserialize
        System.out.println();
        System.out.println("Testing Serialize and Deserialize");
        System.out.println("Serialize");
        String json = i.serialize();
        System.out.println(json);

        System.out.println("Deserialize");
        Inventory k = Inventory.deserialize(json);
        System.out.println(k.listItems());

        //Test isListValid Method
        System.out.println();
        System.out.println("Testing isListValid Method");
        ArrayList<Item> data_invalid = new ArrayList<Item>();
        data_invalid.add(item1);
        data_invalid.add(item2);
        data_invalid.add(item3);
        data_invalid.add(item3);

        try{
            k = new Inventory(data_invalid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
