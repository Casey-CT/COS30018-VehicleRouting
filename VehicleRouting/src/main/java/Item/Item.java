package Item;

public class Item {
    private String c_loc;
    private String d_loc;
    private int weight;
    private int size;

    //Normal Item Constructor
    public Item(String c, String d, int w, int s) {
        c_loc = c;
        d_loc = d;
        weight = w;
        size = s;
    }

    //TO DO
    //Add skipping of escaped ":" characters - should be something along the lines of "(?<!\:):"

    //Constructor to create from JSON
    public Item(String JSON){
        //Set some default values, just in case it fails
        c_loc = "";
        d_loc = "";
        weight = 0;
        size = 0;

        String colon_regex = ":";
        String comma_regex = ",";

        if(!JSON.isEmpty()){
            try{
                String[] strings = JSON.split(":",2);

                //Test
                //System.out.println(strings[0]);

                if(strings[0].equals("Item")){
                    //Remove First "{" and Last "}"
                    if(strings[1] != null) {

                        //Test
                        System.out.println(strings[1]);

                        //Testing an alternate method
                        int i = strings[1].indexOf("{");
                        int j = strings[1].lastIndexOf("}");

                        System.out.println(i + " " + j);

                        //Substring Function creates substring from i, to j-1
                        //If we want to cut out the characters at those indexes we need to go to i + 1
                        if(i != -1 && j != -1){
                            strings[1] = strings[1].substring(i + 1, j);
                        }

                        //Honestly this way might be better I dunno
                        //strings[1] = strings[1].substring(1, strings[1].length() - 1);

                        //Test
                        System.out.println(strings[1]);

                        strings = strings[1].split(",");
                        //There are 4 fields
                        if(strings.length == 4) {
                            //Place to store the strings we are about to split, limiting to 2 substrings, just in case
                            //I'm nesting all these if statements, if one fails, no others will run
                            //If all are successful, then values are assigned.
                            //Value's won't be written unless all values are converted successfully.

                            String c_loc_temp;
                            String d_loc_temp;
                            int weight_temp;
                            int size_temp;

                            String[] temp = strings[0].split(":", 2);
                            if(temp[0].equals("Curr_Loc")){
                                c_loc_temp = temp[1];

                                temp = strings[1].split(":", 2);
                                if(temp[0].equals("Dest_Loc")){
                                    d_loc_temp = temp[1];

                                    temp = strings[2].split(":", 2);
                                    if(temp[0].equals("Weight")){
                                        weight_temp = Integer.parseInt(temp[1]);

                                        temp = strings[3].split(":", 2);
                                        if(temp[0].equals("Size")){
                                            size_temp = Integer.parseInt(temp[1]);

                                            c_loc = c_loc_temp;
                                            d_loc = d_loc_temp;
                                            weight = weight_temp;
                                            size = size_temp;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("ERROR WHILE CREATING ITEM FROM: " + JSON);
            }
        }
        else System.out.println("ERROR WHILE CONSTRUCTING ITEM FROM JSON: JSON WAS NULL");
    }

    //Empty Constructor
    public Item() {
        c_loc = "";
        d_loc = "";
        weight = 0;
        size = 0;
    }

    public String Convert() {
        String JSON = "Item:{"
                    + "Curr_Loc:" + c_loc + ","
                    + "Dest_Loc:" + d_loc + ","
                    + "Weight:" + weight + ","
                    + "Size:" + size
                    + "}";

        //For a check
        System.out.println(JSON);

        return JSON;
    }

    @Override
    public String toString() {
        return "Package. Currently at: " + c_loc + ". Going to: " + d_loc + ". Weight: " + weight + " Size: " + size;
    }

    public boolean isEmpty() {
        return (c_loc.isEmpty() && d_loc.isEmpty() && weight == 0 && size == 0);
    }

    public boolean isValid() {
        return !(c_loc.isEmpty() && d_loc.isEmpty());
    }

    public String getC_loc() {
        return c_loc;
    }

    public String getD_loc() {
        return d_loc;
    }

    public int getWeight() {
        return weight;
    }

    public int getSize() {
        return size;
    }
}
