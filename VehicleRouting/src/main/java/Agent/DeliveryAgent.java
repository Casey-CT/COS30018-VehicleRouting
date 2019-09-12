package Agent;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class DeliveryAgent extends Agent {
    private int capacity = 0;

    public int getCapacity() {
        return capacity;
    }

    protected void setup() {
        System.out.println(getAID().getName() + ": I have been created");
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            capacity = Integer.parseInt(args[0].toString());
            System.out.println(getAID().getName() + ": My capacity is: " + capacity);
        } else {
            // Make the agent terminate immediately
            System.out.println("No capacity specified!");
            doDelete();
        }
    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Delivery-agent " + getAID().getName() + " terminating.");
    }

    private class ListenForMessages extends CyclicBehaviour {
        public void action() {
            //MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            //Create a message template for our purposes

            ACLMessage msg = myAgent.receive();       //pass mt to receive()

            if (msg != null) {
                //Message received. Process it.


                //MessageReader mr = new MessageReader();
                //Items[] = mr.Read(msg.getContent());            //assuming we have an Items class

                ACLMessage reply = msg.createReply();

                //if(at least one package has been received && package can be carried (not overloaded)) {
                //  reply.setPerformative(ACLMessage.INFORM);
                //  reply.setContent("package received");
                // }

                //else(no packages received, or package weight will overload the DA capacity)
                //  reply.setPerformative(ACLMessage.REFUSE);
                //  reply.setContent("no packages received" || "cannot carry package, over capacity, dropping package");
            }
            else{
                    block();
                }
            }
        }
    }








}








