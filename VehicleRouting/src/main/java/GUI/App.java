package GUI;

import Agent.DeliveryAgent;
import Agent.MasterRoutingAgent;
import Item.Item;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.domain.JADEAgentManagement.SniffOn;
import jade.tools.sniffer.Sniffer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.SwingUtilities;

//TODO:
// ADD ITEM TO MASTERROUTER'S INVENTORY BY BUTTON
// SNIFFER AGENT (Console output)
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

    private static final int WEIGHT_SIZE_MODIFIER = 20;

    private static final int CANCEL = -1;

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

    private int itemInt = 0;

    //Placing this here as it is used to spin up additional Delivery Agents
    private ContainerController mainCtrl;

    private GUI.MyAgentInterface o2a;

    private ArrayList<GUI.DeliveryAgentInterface> DAo2aList = new ArrayList<>();

    public App() {

        //Redirect Output
        System.setOut(new PrintStream(out));

        //Create Redirected Console Output Text Areas, ScrollPanes and OutputStreams
        JScrollPane appOutputScroll = new JScrollPane(appOutput);
        JScrollPane agentOutputScroll = new JScrollPane(agentOutput);
        JScrollPane snifferOutputScroll = new JScrollPane(snifferOutput);

        JOptionPane.showMessageDialog(null, "Welcome to VehicleRouting");

        nodeCount = getIntValue("Enter Number of Map Nodes:", "", 5, true);

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

            System.out.println("Attempting to Activate Sniffing Agent");

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        JButton MasterAgentButton = new JButton();
        String mraButtonLabel = "Start Master\nAgent Processing";
        MasterAgentButton.setText("<html>" + mraButtonLabel.replaceAll("\\n", "<br>") + "</html>");

        JButton listMasterInventory = new JButton();
        String listMasterInventoryLabel = "List Master\nInventory Items";
        listMasterInventory.setText("<html>" + listMasterInventoryLabel.replaceAll("\\n", "<br>") + "</html>");

        JButton DeliveryAgentButton = new JButton();
        String daButtonLabel = "Create\nDelivery Agent";
        DeliveryAgentButton.setText("<html>" + daButtonLabel.replaceAll("\\n", "<br>") + "</html>");

        JButton CheckAgentButton = new JButton();
        String checkButtonLabel = "Check Delivery\nAgent Status";
        CheckAgentButton.setText("<html>" + checkButtonLabel.replaceAll("\\n", "<br>") + "</html>");

        JButton addItem = new JButton();
        String addItemLabel = "Add Item";
        addItem.setText("<html>" + addItemLabel + "</html>");

        JButton generateItems = new JButton();
        String generateItemsLabel = "Generate a Number\nOf Random Items";
        generateItems.setText("<html>" + generateItemsLabel.replaceAll("\\n", "<br>") + "</html>");

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

                    int capacity = getIntValue("Enter Capacity For Delivery Agent:", "",50, false);

                    if(capacity == CANCEL) {
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

        //3 list masterInventory
        listMasterInventory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea textArea = new JTextArea(15, 20);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(300, 300));

                textArea.append(o2a.listItems());

                JFrame window = new JFrame("Master Router Inventory");
                Dimension frameDimension = new Dimension(300, 300);
                window.setSize(frameDimension);
                window.add(scrollPane);
                window.pack();
                window.setVisible(true);
            }
        });

        //*4: CHECK FOR AGENT DETAILS BUTTON LISTENER
        CheckAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(agentInt == 0) {
                    System.out.println("There Are No Active Delivery Agents On The System");
                    return;
                }

                int agentIndex = getDaIndex("Enter Value Between 1 - " + agentInt + ":");

                if(agentIndex == CANCEL) {
                    System.out.println("Checking of Delivery Agent Status Cancelled");
                    return;
                }

                JTextArea textArea = new JTextArea(15, 20);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(300, 300));

                textArea.append(DAo2aList.get(agentIndex).getData());

                JFrame window = new JFrame("Delivery Agent Status");
                Dimension frameDimension = new Dimension(300, 300);
                window.setSize(frameDimension);
                window.add(scrollPane);
                window.pack();
                window.setVisible(true);
            }
        });

        //5 AddItem Action Listener
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame addItemWindow = new JFrame("Add Item");

                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints gbc = new GridBagConstraints();

                JLabel itemName = new JLabel("Item Name: ");
                JLabel itemDestination = new JLabel("Destination: (Between 1 - " + (nodeCount - 1) + ") ");
                JLabel itemWeight = new JLabel("Weight: ");
                JLabel itemSize = new JLabel("Size: ");

                JTextField itemNameInput = new JTextField();
                itemNameInput.setPreferredSize(new Dimension(75, 20));
                JTextField itemDestinationInput = new JTextField();
                itemDestinationInput.setPreferredSize(new Dimension(75, 20));
                JTextField itemWeightInput = new JTextField();
                itemWeightInput.setPreferredSize(new Dimension(75, 20));
                JTextField itemSizeInput = new JTextField();
                itemSizeInput.setPreferredSize(new Dimension(75, 20));

                JButton addItem = new JButton("Add Item");
                addItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String nameTemp = itemNameInput.getText();
                        String destTemp = itemDestinationInput.getText();
                        String weightTemp = itemWeightInput.getText();
                        String sizeTemp = itemSizeInput.getText();

                        try{
                            if(nameTemp.isEmpty() || destTemp.isEmpty() || weightTemp.isEmpty() || sizeTemp.isEmpty()) {
                                throw new Exception();
                            }

                            int destInt = Integer.parseInt(destTemp);
                            int weightInt = Integer.parseInt(weightTemp);
                            int sizeInt = Integer.parseInt(sizeTemp);

                            if(destInt < 1 || destInt >= nodeCount) {
                                throw new Exception();
                            }

                            Item item = new Item(itemInt, nameTemp, destInt, weightInt, sizeInt);
                            o2a.AddItemToInventory(item);
                            itemInt++;
                            addItemWindow.dispose();
                        } catch(Exception ex) {
                            System.out.println("Invalid Data Entered. Item Not Created.");
                            return;
                        }
                    }
                });

                Insets inset = new Insets(5, 5, 5, 5);

                addItemWindow.setLayout(layout);

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.insets = inset;
                addItemWindow.add(itemName, gbc);

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.insets = inset;
                addItemWindow.add(itemDestination, gbc);

                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.insets = inset;
                addItemWindow.add(itemWeight, gbc);

                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.insets = inset;
                addItemWindow.add(itemSize, gbc);

                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.insets = inset;
                addItemWindow.add(itemNameInput, gbc);

                gbc.gridx = 1;
                gbc.gridy = 1;
                gbc.insets = inset;
                addItemWindow.add(itemDestinationInput, gbc);

                gbc.gridx = 1;
                gbc.gridy = 2;
                gbc.insets = inset;
                addItemWindow.add(itemWeightInput, gbc);

                gbc.gridx = 1;
                gbc.gridy = 3;
                gbc.insets = inset;
                addItemWindow.add(itemSizeInput, gbc);

                gbc.gridx = 1;
                gbc.gridy = 4;
                gbc.insets = inset;
                addItemWindow.add(addItem, gbc);

                addItemWindow.setSize(300, 250);
                addItemWindow.setVisible(true);
            }
        });

        //5 GenerateItems Action Listener
        generateItems.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int count = getIntValue("Enter Number Of Items to Generate", "", 1, false);

                if(count == CANCEL) {
                    return;
                }

                Random r = new Random();

                for(int i = 0; i < count; i++) {
                    Item item = new Item(itemInt, "Item" + itemInt, r.nextInt(nodeCount - 1) + 1, r.nextInt(WEIGHT_SIZE_MODIFIER - 1) + 1, r.nextInt(WEIGHT_SIZE_MODIFIER - 1) + 1);
                    itemInt++;

                    o2a.AddItemToInventory(item);
                }
            }
        });

        JFrame appFrame = new JFrame("Vehicle Routing App");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        appFrame.setLayout(layout);

        JLabel heading = new JLabel("<html><h1><strong><i>Vehicle Routing</i></strong></h1><hr></html>");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        appFrame.add(heading, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 0, 0);
        MasterAgentButton.setPreferredSize(new Dimension(150, 45));
        appFrame.add(MasterAgentButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 0, 0);
        listMasterInventory.setPreferredSize(new Dimension(150, 45));
        appFrame.add(listMasterInventory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 0, 0);
        DeliveryAgentButton.setPreferredSize(new Dimension(150, 45));
        appFrame.add(DeliveryAgentButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 0, 0);
        CheckAgentButton.setPreferredSize(new Dimension(150, 45));
        appFrame.add(CheckAgentButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 0, 0);
        addItem.setPreferredSize(new Dimension(150, 45));
        appFrame.add(addItem, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 10, 10, 0);
        generateItems.setPreferredSize(new Dimension(150, 45));
        appFrame.add(generateItems, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.insets = new Insets(10, 10, 0, 10);
        appOutputScroll.setPreferredSize(new Dimension(700, 150));
        appFrame.add(appOutputScroll, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridheight = 2;
        gbc.insets = new Insets(10, 10, 0, 10);
        agentOutputScroll.setPreferredSize(new Dimension(700, 150));
        appFrame.add(agentOutputScroll, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridheight = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        snifferOutputScroll.setPreferredSize(new Dimension(700, 100));
        appFrame.add(snifferOutputScroll, gbc);

        appFrame.setSize(0, 0);
        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        try {
            App app = new App();
        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }

    //Creates an input window that requires an input from the user, using the message + additionalMessage and initialValue parameters
    //
    //Input is validated, if valid
    //      -The value is returned
    //      -Otherwise return another call of this method
    //
    //If the required parameter is true
    //      -Cancelling the dialog returns another call of this method
    //      -Otherwise returns the CANCEL constant
    public int getIntValue(String message, String additionalMessage, int initialValue, boolean required) {
        String input = JOptionPane.showInputDialog(null, additionalMessage + message, initialValue);
        try{
            if(!input.isEmpty()) {
               int result = Integer.parseInt(input);
               if(result > 0) {
                   return result;
               }
               else {
                   return getIntValue(message, "Number Cannot Be Negative.\n", initialValue, required);
               }
            }
            else {
                return getIntValue(message,"Do Not Leave Blank.\n", initialValue, required);
            }
        } catch(NumberFormatException ex) {
            return getIntValue(message, "Enter a Valid Number.\n", initialValue, required);
        } catch(NullPointerException ex) {
            if(required) {
                return getIntValue(message ,"Number of Nodes is Required.\n", initialValue, required);
            }
            else {
                return CANCEL;
            }
        }
    }

    //Works in the same way as getIntValue(), but always returns the CANCEL_DA constant if cancel is pressed
    //Additionally, inputted value must be <= agentInt and >= 1
    //Value returned is inputted value - 1, to work appropriately with the index
    public int getDaIndex(String message) {
        String input = JOptionPane.showInputDialog(null, message, null);
        try{
            if(!input.isEmpty()) {
                int result = Integer.parseInt(input);
                if(result <= agentInt && result >= 1) {
                    return result - 1;
                }
                else {
                    return getDaIndex("Result Must Be Less Than Or Equal To " + agentInt + "\nEnter Value Between 1 - " + agentInt + ":");
                }
            }
            else {
                return getDaIndex("Do Not Leave Blank.\nEnter Value Between 1 - " + agentInt + ":");
            }
        } catch(NumberFormatException ex) {
            return getDaIndex("Enter a Valid Number.\nEnter Value Between 1 - " + agentInt + ":");
        } catch(NullPointerException ex) {
            return CANCEL;
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
