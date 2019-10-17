package Item.Test;

import Item.Item;

public class ItemTest {
    public static void main(String[] args) {
        Item i = new Item(1, "Name", 2, 5, 7);
        String json = i.serialize();
        System.out.println("Testing Serializing");
        System.out.println(json);

        Item j = Item.deserialize(json);
        System.out.println();
        System.out.println("Testing Deserializing");
        System.out.println("Item J: " + j.getId() + " " + j.getName());
    }
}
