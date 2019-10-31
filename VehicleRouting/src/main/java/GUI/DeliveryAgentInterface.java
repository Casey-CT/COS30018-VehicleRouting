package GUI;

import jade.core.AID;

import java.io.OutputStream;

public interface DeliveryAgentInterface {
    String getData();
    void OverwriteOutput(OutputStream out);
    AID getAgentName();
}
