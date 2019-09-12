package DeliveryPath.Test;

import DeliveryPath.Path;

public class PathTest {
    public static void main(String[] args) {
        Path p = new Path(new int[]{1, 2, 5, 4, 7}, new int[]{3, 6, 7, 4, 9});

        //Testing Serialization
        System.out.println("Testing Serialization");
        String json = p.serialize();
        System.out.println(json);

        //Testing Deserialization
        System.out.println("Testing Deserialization");
        Path q = Path.deserialize(json);

        q.traversePath();
        System.out.println("Next Location: " + q.getNextLocation());
        System.out.println("Next Distance: " + q.getNextDistance());


        //Testing Constructor Exceptions
        try{
            System.out.println("Testing Constructor with Invalid Values");
            p = new Path(new int[]{1}, new int[]{1, 4});
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try{
            System.out.println("Testing Constructor with Empty Values");
            p = new Path(new int[]{}, new int[]{});
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try{
            System.out.println("Testing Constructor with NULL Values");
            p = new Path(null, null);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
