package GUI;

import Agent.DeliveryAgent;
import Agent.MasterRoutingAgent;
import jade.Boot;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
//import oracle.jrockit.jfr.JFR;
//import sun.management.resources.agent;


//import oracle.jrockit.jfr.JFR;


public class App {
    private int n;

    final JFrame frame = new JFrame();
    final JFrame secondFrame = new JFrame();
    final JFrame thirdFrame = new JFrame();
    ArrayList<JButton> buttons = new ArrayList<>();



//    DAo2aList.add();



    private JButton MasterAgentButton;
    private JButton DeliveryAgentButton;
    private JButton CheckAgentButton;
    private JTextField DACapacity;
//    JLayeredPane DACapacityPane = new JLayeredPane();
    private JPopupMenu DACapacityPopup = new JPopupMenu();

    private int capacityInt;

    private JPanel mainPanel;
    private int agentInt;
    private MyAgentInterface o2a;


    private DeliveryAgentInterface DAo2a;
    private ArrayList<GUI.DeliveryAgentInterface> DAo2aList = new ArrayList<>();

    public App() throws StaleProxyException, InterruptedException {
        //Init
        Runtime rt = Runtime.instance();
        Profile pMain = new ProfileImpl(null, 1099, null);
        ContainerController mainCtrl = rt.createMainContainer(pMain);
        Thread.sleep(10000);
        AgentController AgentCtrl = mainCtrl.createNewAgent("MasterRoutingAgent", MasterRoutingAgent.class.getName(), new Object[0]);

        try {
            System.out.println(AgentCtrl.getName() + ": Activating RoutingAgent");

            AgentCtrl.start();
            Thread.sleep(2000);
//            DAo2a =
            o2a = AgentCtrl.getO2AInterface(MyAgentInterface.class);

//            GUI.MyAgentInterface mAIObject =


        }
        catch (StaleProxyException | InterruptedException e) {
            e.printStackTrace();
        }


        MasterAgentButton = new JButton();
        MasterAgentButton.setText("MasterAgent");
        DeliveryAgentButton = new JButton();
        DeliveryAgentButton.setText("DeliveryAgent");
        CheckAgentButton = new JButton();
        CheckAgentButton.setText("CheckDeliveryAgent");
        buttons.add(MasterAgentButton);
        buttons.add(DeliveryAgentButton);
        buttons.add(CheckAgentButton);



//        DACapacity = new JTextField(20);


        MasterAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AgentCtrl.start();
                    System.out.println(AgentCtrl.getName() + ": Beginning MasterAgent");
                    o2a.StartMasterAgent();
                }
                catch (StaleProxyException ex) {
                    ex.printStackTrace();
                }
            }
        });


        // *2: DELIVERY AGENT ACTION LISTENER

        DeliveryAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    agentInt++;
                    String temp = JOptionPane.showInputDialog(frame,"Enter Capacity", null);
                    Thread.sleep(10);
                    capacityInt = Integer.parseInt(temp);
                    Object[] args = {(Integer) capacityInt};

                    //ASSIGNING DAO2A AND THEN ADDING IT TO THE LIST
                    AgentController DACtrl = mainCtrl.createNewAgent("DeliveryAgent" + agentInt, DeliveryAgent.class.getName(), args);
                    DACtrl.start();
                    Thread.sleep(5000);
                    DAo2a = DACtrl.getO2AInterface(DeliveryAgentInterface.class);

                    DAo2aList.add(DAo2a);


//                    final JButton daButton = new JButton();
//                    buttons.add(daButton);
//                    secondFrame.add(daButton);




                System.out.println(DACtrl.getName() + agentInt + "Created DeliveryAgent");
                }
                catch (StaleProxyException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });


        //*3: CHECK FOR AGENT DETAILS BUTTON LISTENER
        CheckAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AMSAgentDescription[] agents = null;


//                for (AgentController ac : deliveryAgents) {
//                    final JButton btn = new JButton();
//                    buttons.add(btn);
//                    btn.addActionListener(new ActionListener() {
                        //GET THE INFORMATION FROM THE LISTENER
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            JLabel deliveryLabel = new JLabel();
//                            for (AgentController ac : deliveryAgents) {
//
//                            }
//                            deliveryLabel.setText("Variable regarding info");
//                            thirdFrame.setVisible(true);
//                        }
//                    });
//                }
                GridBagLayout gbl = new GridBagLayout();

                JPanel gridButtons = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();

//                secondFrame.add();
                Dimension frameDimension = new Dimension(300, 300);
                secondFrame.setSize(frameDimension);
                secondFrame.setVisible(true);



//                try {
//                    String temp = JOptionPane.showInputDialog(frame, "Select Agent", null);
////                    foreach(Agent in mainCtrl.);
//
//                }
//                catch (Exception ex1){
//
//                }



//                try {
//                    SearchConstraints c = new SearchConstraints();
//                    c.setMaxResults((long) -1);
//                    agents = AMSService.search(mainCtrl, new AMSAgentDescription(), c);
//                    agents = AMSService.search(mainCtrl, new AMSAgentDescription(), c);
//                } catch (Exception ex2) {

//                }
                try {
                    Thread.sleep(10);
                    //capacityInt = Integer.parseInt(temp);
                    Object[] args = {(Integer) capacityInt};

                    int i=0;
                    String s = "";
                    while(i < DAo2aList.size()) {
                        s = DAo2aList.get(i).getData();
//                        s + " " + DAo2aList.get(i).getData();
//                        s + " " + toString(DAo2aList[i].getData());
//                        DAo2aList
                        System.out.println(s);
                    }
                    System.out.println(DAo2aList);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });


        mainPanel = new JPanel();

        GridBagLayout gbl = new GridBagLayout();

        JPanel gridButtons = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;

        mainPanel.add(new JLabel("<html><h1><strong><i>Vehicle Routing</i></strong></h1><hr></html>"), gbc);

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel buttons = new JPanel(new GridBagLayout());

        mainPanel.add(MasterAgentButton, gbc);
        mainPanel.add(DeliveryAgentButton, gbc);
        mainPanel.add(CheckAgentButton, gbc);


    }
    protected void paramStart() {
        String[] param = new String[1];
        param[0] = "-gui";
        //param[1] = "-agents";
        //param[2] = "drFoo:HelloWorldAgent";
        Boot.main(param);
    }

    public static void main(String[] args) {

        try {
            JFrame frame = new JFrame("App");

            frame.setContentPane(new App().mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Dimension frameDimension = new Dimension(300, 300);

            frame.setSize(frameDimension);
//            frame.pack();
            frame.setVisible(true);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }


    }


}