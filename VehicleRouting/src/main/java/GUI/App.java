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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import org.w3c.dom.Text;
//import oracle.jrockit.jfr.JFR;
//import sun.management.resources.agent;


//import oracle.jrockit.jfr.JFR;

//TODO:
// ADD ITEM TO MASTERROUTER'S INVENTORY BY BUTTON
// MAPPING POPUP GUI INTERFACE (REPEATED POPUPS)
// SNIFFER AGENT (Console output)
// ASK ABOUT 40% EXTRA MARKS ON EXTENSION
// EXTENSION: IMAGE OF MAP TO GUI (Alex actually needs help send help please)
// EXTENSION: Animations
// Comment this code
public class App {

    //Map Constants
    //MIN_CON_MODIFIER is multiplied by nodeCount to get the minimum number of connection in the generated graph
    //MAX_CON_MODIFIER is multiplied by nodeCount to get the maximum number of connection in the generated graph
    //MIN_DIST is the minimum distance between any two nodes
    //MAX_DIST is the maximum distance between any two nodes
    private static final int MIN_CON_MODIFIER = 1;
    private static final int MAX_CON_MODIFIER = 3;
    private static final int MIN_DIST = 5;
    private static final int MAX_DIST = 15;

    private static final int CANCEL_DA = -1;

    //TODO: Fill in Sniffer Text
    private static final String MRA_TEXT = "MasterRoutingAgent";
    private static final String DA_TEXT = "DeliveryAgent";
    private static final String SNIFFER_TEXT = "Sniffer";

    //Redirected Output Stream
    private TextUpdater textUpdater = new TextUpdater();

    private OutputStream out = new OutputStream() {

        @Override
        public void write(int b) throws IOException {
            textUpdater.updateText(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b) throws IOException {
            textUpdater.updateText(new String(b, 0, b.length));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            textUpdater.updateText(new String(b, off, len));
        }
    };

    private JTextArea appOutput = new JTextArea(15, 50);
    private JTextArea agentOutput = new JTextArea(15, 50);
    private JTextArea snifferOutput = new JTextArea(15, 50);

    //Number of Nodes in the current map
    //User this to make sure invalid items aren't added to the MRA's masterInventory
    private int nodeCount = 0;

    private int agentInt = 0;

    //Placing this here as it is used to spin up additional Delivery Agents
    private ContainerController mainCtrl;

    private GUI.MyAgentInterface o2a;

    private ArrayList<GUI.DeliveryAgentInterface> DAo2aList = new ArrayList<>();

    public App() {

        //Create Redirected Console Output Text Areas, ScrollPanes and OutputStreams
        JScrollPane appOutputScroll = new JScrollPane(appOutput);
        JScrollPane agentOutputScroll = new JScrollPane(agentOutput);
        JScrollPane snifferOutputScroll = new JScrollPane(snifferOutput);

        JOptionPane.showMessageDialog(null, "Welcome to VehicleRouting");

        nodeCount = getNodeCount("Enter Number of Map Nodes:");

        try {
            Runtime rt = Runtime.instance();
            Profile pMain = new ProfileImpl(null, 8888, null);
            mainCtrl = rt.createMainContainer(pMain);
            Thread.sleep(1000);
            AgentController AgentCtrl = mainCtrl.createNewAgent(MRA_TEXT, MasterRoutingAgent.class.getName(), new Object[0]);

            System.out.println("Attempting to Activate RoutingAgent");



            AgentCtrl.start();
            Thread.sleep(1000);
            o2a = AgentCtrl.getO2AInterface(GUI.MyAgentInterface.class);
            o2a.OverwriteOutput(out);


            o2a.GenerateMap(nodeCount, MIN_DIST, MAX_DIST, nodeCount * MIN_CON_MODIFIER, nodeCount * MAX_CON_MODIFIER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        JButton MasterAgentButton = new JButton();
        String mraButtonLabel = "Start Master\nAgent Processing";
        MasterAgentButton.setText("<html>" + mraButtonLabel.replaceAll("\\n", "<br>") + "</html>");
        JButton DeliveryAgentButton = new JButton();
        String daButtonLabel = "Create\nDelivery Agent";
        DeliveryAgentButton.setText("<html>" + daButtonLabel.replaceAll("\\n", "<br>") + "</html>");
        JButton CheckAgentButton = new JButton();
        String checkButtonLabel = "Check Delivery\nAgent Status";
        CheckAgentButton.setText("<html>" + checkButtonLabel.replaceAll("\\n", "<br>") + "</html>");

        MasterAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println("Master Agent Beginning Processing...");
                    o2a.StartMasterAgent();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        // *2: DELIVERY AGENT ACTION LISTENER

        DeliveryAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(mainCtrl == null) {
                        throw new Exception("Jade Container Controller is NULL.");
                    }

                    int capacity = getDaCapacity("Enter Capacity For Delivery Agent:");

                    if(capacity == CANCEL_DA) {
                        System.out.println("Cancelled Creation of DA");
                        return;
                    }
                    Object[] args = {capacity};

                    agentInt++;

                    //ASSIGNING DAO2A AND THEN ADDING IT TO THE LIST
                    AgentController DACtrl = mainCtrl.createNewAgent(DA_TEXT + agentInt, DeliveryAgent.class.getName(), args);
                    DACtrl.start();
                    Thread.sleep(500);
                    DeliveryAgentInterface DAo2a = DACtrl.getO2AInterface(DeliveryAgentInterface.class);

                    //Overwrite Console Output
                    DAo2a.OverwriteOutput(out);

                    DAo2aList.add(DAo2a);

                    System.out.println("Created " + DACtrl.getName() + " From GUI");
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        //*3: CHECK FOR AGENT DETAILS BUTTON LISTENER
        CheckAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AMSAgentDescription[] agents = null;

                GridBagLayout gbl = new GridBagLayout();

                JPanel gridButtons = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();

                JFrame window = new JFrame("Check DA");

                Dimension frameDimension = new Dimension(300, 300);
                window.setSize(frameDimension);
                window.setVisible(true);

                try {
                    Thread.sleep(10);

                    //capacityInt = Integer.parseInt(temp);
                    System.out.println(DAo2aList.size());

                    for(DeliveryAgentInterface d: DAo2aList) {
                        System.out.println(d.getData());
                    }

                    // System.out.println(DAo2aList);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JFrame appFrame = new JFrame("App");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        appFrame.setLayout(layout);

        JLabel heading = new JLabel("<html><h1><strong><i>Vehicle Routing</i></strong></h1><hr></html>");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.ipadx = 50;
        gbc.ipady = 50;

        appFrame.add(heading, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        appFrame.add(MasterAgentButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        appFrame.add(DeliveryAgentButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        appFrame.add(CheckAgentButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        appOutputScroll.setPreferredSize(new Dimension(650, 100));
        appFrame.add(appOutputScroll, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        agentOutputScroll.setPreferredSize(new Dimension(650, 100));
        appFrame.add(agentOutputScroll, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        snifferOutputScroll.setPreferredSize(new Dimension(650, 100));
        appFrame.add(snifferOutputScroll, gbc);

        appFrame.setSize(0, 0);
        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        try {
            App app = new App();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }


    }

    //Creates an input window that requires an input from the user
    //Pressing cancel, or inputting an invalid response causes another window to appear
    //Returns a valid int
    public int getNodeCount(String message) {
        String input = JOptionPane.showInputDialog(null, message, "5");
        try{
            if(!input.isEmpty()) {
               int result = Integer.parseInt(input);
               return result;
            }
            else {
                return getNodeCount("Do Not Leave Blank.\nEnter Number of Map Nodes:");
            }
        } catch(NumberFormatException ex) {
            return getNodeCount("Enter a Valid Number.\nEnter Number of Map Nodes:");
        } catch(NullPointerException ex) {
            return getNodeCount("Number of Nodes is Required.\nEnter Number of Map Nodes:");
        }
    }

    //Works in the same way as getNodeCount(), but returns the CANCEL_DA constant if cancel is pressed
    public int getDaCapacity(String message) {
        String input = JOptionPane.showInputDialog(null, message, "50");
        try{
            if(!input.isEmpty()) {
                int result = Integer.parseInt(input);
                return result;
            }
            else {
                return getDaCapacity("Do Not Leave Blank.\nEnter Capacity For Delivery Agent:");
            }
        } catch(NumberFormatException ex) {
            return getDaCapacity("Enter a Valid Number.\nEnter Capacity For Delivery Agent:");
        } catch(NullPointerException ex) {
            return CANCEL_DA;
        }
    }

    //Test Text Area Methods
    public class TextUpdater {
        JTextArea prevTarget;

        private void updateText(final String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JTextArea target = appOutput;

                    if(text.contains(DA_TEXT)) {
                        target = agentOutput;
                    }
                    else if(text.contains(SNIFFER_TEXT)) {
                        target = snifferOutput;
                    }

                    if(text.equals(System.lineSeparator())) {
                        target = prevTarget;
                    }
                    else {
                        prevTarget = target;
                    }

                    target.append(text);
                }
            });
        }
    }
}
