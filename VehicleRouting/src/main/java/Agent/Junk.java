package Agent;

import GUI.DeliveryAgentInterface;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

public class Junk {


    public static void main(String[] args) throws InterruptedException, StaleProxyException {
        
        Runtime rt = Runtime.instance();
        Profile pMain = new ProfileImpl(null, 8888, null);
        ContainerController mainCtrl = rt.createMainContainer(pMain);
        Thread.sleep(2000);


        int agentInt = 0;
        GUI.DeliveryAgentInterface DAo2a;
        ArrayList<DeliveryAgentInterface> DAo2aList = new ArrayList<>();
        Object[] argss = {(Integer) 50};
        AgentController DACtrl = mainCtrl.createNewAgent("DeliveryAgent" + agentInt, DeliveryAgent.class.getName(), argss);
        Thread.sleep(1000);
        DACtrl.start();
        Thread.sleep(1000);
        DAo2a = DACtrl.getO2AInterface(DeliveryAgentInterface.class);

        DAo2aList.add(DAo2a);

        System.out.println(DAo2aList.size());

        for(DeliveryAgentInterface d: DAo2aList) {
            System.out.println(d.getData());
        }
    }
}
