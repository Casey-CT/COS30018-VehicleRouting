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


//import oracle.jrockit.jfr.JFR;


public class App {
    private int n;

    final JFrame frame = new JFrame();
    ArrayList<JButton> buttons = new ArrayList<>();

    private JButton MasterAgentButton;
    private JButton DeliveryAgentButton;
    private JTextField DACapacity;
//    JLayeredPane DACapacityPane = new JLayeredPane();
    private JPopupMenu DACapacityPopup = new JPopupMenu();

    private int capacityInt;

    private JPanel mainPanel;
    private int agentInt;
    private MyAgentInterface o2a;

    public App() throws StaleProxyException {
        //Init
        Runtime rt = Runtime.instance();
        Profile pMain = new ProfileImpl(null, 8888, null);
        ContainerController mainCtrl = rt.createMainContainer(pMain);
        AgentController AgentCtrl = mainCtrl.createNewAgent("MasterRoutingAgent", MasterRoutingAgent.class.getName(), new Object[0]);

        try {
            System.out.println(AgentCtrl.getName() + ": Activating RoutingAgent");

            AgentCtrl.start();
            Thread.sleep(2000);
            MyAgentInterface o2a = AgentCtrl.getO2AInterface(MyAgentInterface.class);
            o2a.StartMasterAgent();


        }
        catch (StaleProxyException | InterruptedException e) {
            e.printStackTrace();
        }


        MasterAgentButton = new JButton();
        MasterAgentButton.setText("MasterAgent");
        DeliveryAgentButton = new JButton();
        DeliveryAgentButton.setText("DeliveryAgent");
        buttons.add(MasterAgentButton);
        buttons.add(DeliveryAgentButton);



//        DACapacity = new JTextField(20);


//        MasterAgentButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    AgentController MACtrl = mainCtrl.createNewAgent("MasterAgent", MasterRoutingAgent.class.getName(), new Object[0]);
//                    MACtrl.start();
//                    System.out.println(MACtrl.getName() + ": Beginning MasterAgent");
//                    o2a.StartMasterAgent();
//                }
//                catch (StaleProxyException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
        DeliveryAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    agentInt++;
                    String temp = JOptionPane.showInputDialog(frame,"Enter Capacity", null);
                    Thread.sleep(10);
                    capacityInt = Integer.parseInt(temp);
                    Object[] args = {(Integer) capacityInt};

                    AgentController DACtrl = mainCtrl.createNewAgent("DeliveryAgent" + agentInt, DeliveryAgent.class.getName(), args);

                    DACtrl.start();



                System.out.println(DACtrl.getName() + agentInt + "Created DeliveryAgent");
                }
                catch (StaleProxyException | InterruptedException ex) {
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
