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
        Thread.sleep(10000);


        int agentInt = 0;
        GUI.DeliveryAgentInterface DAo2a;
        ArrayList<DeliveryAgentInterface> DAo2aList = new ArrayList<>();
        Object[] argss = {(Integer) 50};
        AgentController DACtrl = mainCtrl.createNewAgent("DeliveryAgent" + agentInt, DeliveryAgent.class.getName(), argss);
        Thread.sleep(5000);
        DACtrl.start();
        Thread.sleep(5000);
        DAo2a = DACtrl.getO2AInterface(DeliveryAgentInterface.class);

        DAo2aList.add(DAo2a);

        int i=0;
        String s="";
        while(i < DAo2aList.size()) {
            s = DAo2aList.get(i).getData();
            s = " " + DAo2aList.get(i).getData();
            //s = " " + toString(DAo2aList[i].getData());
                        
            System.out.println(s);
        }
    }
}
