package Communication;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Iterator;

//Set of constants, used to make sure messages sent from Agents, and messages that are processed are consistent.
public class Message {
    public static final String MESSAGE = "MESSAGE OUTPUT";
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
    public static final String COMPLETE = "COMPLETE";
    public static final String RETURN = "RETURN";

    //Outputs to System.out, a String represention of supplied ACLMessage
    public static void outputMessage(ACLMessage message) {
        StringBuilder builder = new StringBuilder();

        builder.append(MESSAGE);
        builder.append(": FROM - ");
        builder.append(message.getSender().getLocalName());
        builder.append(". TO - ");
        for(Iterator receivers = message.getAllReceiver(); receivers.hasNext();) {
            AID agent = (AID) receivers.next();
            builder.append(agent.getLocalName());
            if(receivers.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(". PERFORMATIVE - ");

        switch(message.getPerformative()) {
            case ACLMessage.INFORM:
                builder.append("INFORM");
                break;

            case ACLMessage.REQUEST:
                builder.append("REQUEST");
                break;

            case ACLMessage.FAILURE:
                builder.append("FAILURE");
                break;

            default:
                builder.append("ERROR");
        }

        builder.append(". CONTENT: ");
        builder.append(message.getContent());
        builder.append(".");

        System.out.println(builder.toString());
    }
}