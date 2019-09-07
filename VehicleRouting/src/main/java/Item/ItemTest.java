package Item;

public class ItemTest {

    public static void main(String[] args) {

        System.out.println("Testing Constructor");
        Item i = new Item("Location 1", "Location 2", 5, 2);
        System.out.println(i.toString());

        System.out.println();
        System.out.println("Testing Empty Constructor");
        Item j = new Item();
        System.out.println(j.toString());

        System.out.println();
        System.out.println("Testing Conversion");
        String c = i.Convert();

        System.out.println();
        System.out.println("Testing Re-Conversion");
        Item k = new Item(c);
        System.out.println(k.toString());
        System.out.println(k.isValid());

        System.out.println();
        System.out.println("Testing Re-Conversion with Faulty Data");
        System.out.println("Should Return an Empty Object");
        Item l = new Item("Item:{Curr_Loc:Place1,Dest_Loc:Place2,Weight:6,Size:B}");
        System.out.println(l.isEmpty());
    }
}
