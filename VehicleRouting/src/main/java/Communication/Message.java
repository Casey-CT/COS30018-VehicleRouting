package Communication;

//Set of constants, used to make sure messages sent from Agents, and messages that are processed are consistent.
public class Message {
    public static final String STOP = "STOP";
    public static final String START = "START";
    public static final String STATUS = "STATUS";
    public static final String PATH = "PATH";
    public static final String PATH_SUCCESS = "PATH_SUCCESS";
    public static final String PATH_FAILURE = "PATH_FAILURE";
    public static final String INVENTORY = "INVENTORY";
    public static final String INVENTORY_SUCCESS = "INVENTORY_SUCCESS";
    public static final String INVENTORY_FAILURE = "INVENTORY_FAILURE";
    public static final String ARRIVE = "ARRIVE";
    public static final String DELIVERED = "DELIVERED";
    public static final String ERROR = "ERROR";
}