package GUI;

import Agent.DeliveryAgent;
import Agent.MasterRoutingAgent;
import Communication.Message;
import DeliveryPath.Path;
import Item.Item;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.SwingUtilities;

//TODO:
// EXTENSION: IMAGE OF MAP TO GUI + Lines For DA Path
// Comment this code
public class App {

    //Map Constants
    //MIN_CON_MODIFIER is used to calculate the minimum percentage of filled connections
    //MAX_CON_MODIFIER is used to calculate the maximum percentage of filled connections
    //MIN_DIST is the minimum distance between any two nodes
    //MAX_DIST is the maximum distance between any two nodes
    private static final int MIN_CON_MODIFIER = 50;
    private static final int MAX_CON_MODIFIER = 75;
    private static final int MIN_DIST = 5;
    private static final int MAX_DIST = 15;

    //Item Creation Constants
    //WEIGHT_SIZE_MODIFIER is used with a Random.nextInt() to generate weights and sizes for randomly generated items
    private static final int WEIGHT_SIZE_MODIFIER = 20;

    //Window Form Constants
    //CANCEL is returned to cancel out of non-required input windows
    private static final int CANCEL = -1;

    //File Name Constants
    //ITEM_DATA_FILE is the text file where String representation of items are stored
    //MAP_DATA_FILE is the text file where String representation of MasterRoutingAgent.mapData is stored
    //OPTION_FILE is the text file where Automatic Loading preference is stored
    //FILE_DELIMITER is the delimiter used by String.split() when loading Items
    private static final String ITEM_DATA_FILE = "items.txt";
    private static final String MAP_DATA_FILE = "map.txt";
    private static final String OPTION_FILE = "options.txt";
    private static final String FILE_DELIMITER = ";";

    //Agent Name Constants
    //MRA_TEXT is the name given to the MasterRoutingAgent, and is used to filter its output into the correct JTextArea
    //DA_TEXT is part of the name given to DeliveryAgents, and is used to filter its output into the correct JTextArea
    private static final String MRA_TEXT = "MasterRoutingAgent";
    private static final String DA_TEXT = "DeliveryAgent";

    //Redirected OutputStream Objects
    private TextUpdater textUpdater = new TextUpdater();

    //New OutputStream for redirecting output to JTextArea elements
    //Uses textUpdater to redirect output to different JTextAreas
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

    //Different JTextAreas for Redirected Output
    //appOutput: GUI Output and MasterRoutingAgent Output
    //agentOutput: Delivery Agent Output
    //snifferOutput: ACLMessage Output
    private JTextArea appOutput = new JTextArea(15, 50);
    private JTextArea agentOutput = new JTextArea(15, 50);
    private JTextArea snifferOutput = new JTextArea(15, 50);

    //GraphVis Object, Used to Draw Graph and DA Path
    private GraphVis graphVis;

    //Number of Nodes in the current map
    //Used this to make sure invalid items aren't added to the MRA's masterInventory
    private int nodeCount = 0;

    //Current number of spun up Delivery Agents
    //Incremented every time a new Delivery Agent is added
    private int agentInt = 0;

    //Total number of packages added to MasterRoutingAgent
    //Incremented every time a new package is created
    //Is NOT decremented when packages are removed from MasterRoutingAgent's inventory
    private int itemInt = 0;

    //ContainerController, value set while Starting up JADE
    //Used to spin up additional Delivery Agents
    private ContainerController mainCtrl;

    //Interface, bound to MasterRoutingAgent
    private GUI.MyAgentInterface o2a;

    //Interface ArrayList. Interface bound to each spun up Delivery Agent is added to this list
    private ArrayList<GUI.DeliveryAgentInterface> DAo2aList = new ArrayList<>();

    public App() {

        //Redirect Output
        System.setOut(new PrintStream(out));

        //Start Up Jade
        try {
            Runtime rt = Runtime.instance();
            Profile pMain = new ProfileImpl(null, 8888, null);
            mainCtrl = rt.createMainContainer(pMain);
            Thread.sleep(1000);
            AgentController AgentCtrl = mainCtrl.createNewAgent(MRA_TEXT, MasterRoutingAgent.class.getName(), new Object[0]);

            System.out.println("Attempting to Activate RoutingAgent");

            //Start Master Routing Agent
            AgentCtrl.start();
            Thread.sleep(1000);

            //Bind Interface and OverWrite Output
            o2a = AgentCtrl.getO2AInterface(GUI.MyAgentInterface.class);
            o2a.OverwriteOutput(out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Welcome Prompt
        JOptionPane.showMessageDialog(null, "Welcome to VehicleRouting");

        //Check whether to Auto load data or not
        if(getPreferences()) {
            loadMap();
            loadItems();
        } else {
            //Get a Node Count, and randomly generate a Map
            nodeCount = getIntValue("Enter Number of Map Nodes:", "", 5, true);
            o2a.GenerateMap(nodeCount, MIN_DIST, MAX_DIST, ((nodeCount * nodeCount) * MIN_CON_MODIFIER) / 100, ((nodeCount * nodeCount) * MAX_CON_MODIFIER) / 100);
            //System.out.println("DEBUG. Number of Nodes: " + nodeCount + " Minimum Connections: " + ((nodeCount * nodeCount) * MIN_CON_MODIFIER) / 100 + " Maximum Connections: " + ((nodeCount * nodeCount) * MAX_CON_MODIFIER) / 100);
        }

        //Create GUI Buttons
        //MRA Processing Button
        JButton MasterAgentButton = new JButton();
        String mraButtonLabel = "Start Master\nAgent Processing";
        MasterAgentButton.setText("<html>" + mraButtonLabel.replaceAll("\\n", "<br>") + "</html>");

        //List MRA Inventory Button
        JButton listMasterInventory = new JButton();
        String listMasterInventoryLabel = "List Master\nInventory Items";
        listMasterInventory.setText("<html>" + listMasterInventoryLabel.replaceAll("\\n", "<br>") + "</html>");

        //Create Delivery Agent Button
        JButton DeliveryAgentButton = new JButton();
        String daButtonLabel = "Create\nDelivery Agent";
        DeliveryAgentButton.setText("<html>" + daButtonLabel.replaceAll("\\n", "<br>") + "</html>");

        //Check Delivery Agent Inventory Button
        JButton CheckAgentButton = new JButton();
        String checkButtonLabel = "Check Delivery\nAgent Status";
        CheckAgentButton.setText("<html>" + checkButtonLabel.replaceAll("\\n", "<br>") + "</html>");

        //Add Item Button
        JButton addItem = new JButton();
        String addItemLabel = "Add Item";
        addItem.setText("<html>" + addItemLabel + "</html>");

        //Generate Random Items Button
        JButton generateItems = new JButton();
        String generateItemsLabel = "Generate a Number\nOf Random Items";
        generateItems.setText("<html>" + generateItemsLabel.replaceAll("\\n", "<br>") + "</html>");

        //Load Items From File Button
        JButton loadItemsFile = new JButton();
        String loadItemsFileLabel = "Load Items\nFrom File";
        loadItemsFile.setText("<html>" + loadItemsFileLabel.replaceAll("\\n", "<br>") + "</html>");

        //Options Menu Button
        JButton setOptions = new JButton();
        String setOptionsLabel = "Options";
        setOptions.setText("<html>" + setOptionsLabel + "</html>");

        //Add Action Listeners to Buttons
        //1 - MRA Processing Button
        MasterAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //Tell MasterAgent to Begin Processing
                    System.out.println("Master Agent Beginning Processing...");
                    o2a.StartMasterAgent();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        //2 - Create Delivery Agent Button
        DeliveryAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(mainCtrl == null) {
                        throw new Exception("Jade Container Controller is NULL.");
                    }

                    //Get a Capacity
                    int capacity = getIntValue("Enter Capacity For Delivery Agent:", "",50, false);


                    if(capacity == CANCEL) {
                        System.out.println("Cancelled Creation of DA");
                        return;
                    }
                    Object[] args = {capacity};

                    //Increment Agent Int
                    agentInt++;

                    //Spin Up Delivery Agent, Bind Interface and Add Interface to ArrayList
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

        //3 - List MRA Inventory Button
        listMasterInventory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create New Text Area
                JTextArea textArea = new JTextArea(15, 20);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(300, 300));

                //Add Text to Area
                textArea.append(o2a.listItems());

                //Create Window and Add Text Area to Window
                JFrame window = new JFrame("Master Router Inventory");
                Dimension frameDimension = new Dimension(300, 300);
                window.setSize(frameDimension);
                window.add(scrollPane);
                window.pack();
                window.setVisible(true);
            }
        });

        //4 - Check Delivery Agent Status Button
        CheckAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Make Sure There are DA's to show
                if(agentInt == 0) {
                    System.out.println("There Are No Active Delivery Agents On The System");
                    return;
                }

                //Get a valid index for DAo2aList
                int agentIndex = getDaIndex("Enter Value Between 1 - " + agentInt + ":");

                if(agentIndex == CANCEL) {
                    System.out.println("Checking of Delivery Agent Status Cancelled");
                    return;
                }

                //Create a new Text Area
                JTextArea textArea = new JTextArea(15, 20);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(300, 300));

                //Add Text
                textArea.append(DAo2aList.get(agentIndex).getData());

                //Create a New Window
                JFrame window = new JFrame("Delivery Agent Status");
                Dimension frameDimension = new Dimension(300, 300);
                window.setSize(frameDimension);
                window.add(scrollPane);
                window.pack();
                window.setVisible(true);

                //Set The Path in GraphVis and Tell to Repaint
                graphVis.setPathToDraw(DAo2aList.get(agentIndex).getPath());
                graphVis.repaint();
            }
        });

        //5 - Add Item Button
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create New Window
                JFrame addItemWindow = new JFrame("Add Item");

                //Create Window Layout
                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints gbc = new GridBagConstraints();
                addItemWindow.setLayout(layout);

                //Create Window Labels
                JLabel itemName = new JLabel("Item Name: ");
                JLabel itemDestination = new JLabel("Destination: (Between 1 - " + (nodeCount - 1) + ") ");
                JLabel itemWeight = new JLabel("Weight: ");
                JLabel itemSize = new JLabel("Size: ");

                //Create Window Text Fields
                JTextField itemNameInput = new JTextField();
                itemNameInput.setPreferredSize(new Dimension(75, 20));
                JTextField itemDestinationInput = new JTextField();
                itemDestinationInput.setPreferredSize(new Dimension(75, 20));
                JTextField itemWeightInput = new JTextField();
                itemWeightInput.setPreferredSize(new Dimension(75, 20));
                JTextField itemSizeInput = new JTextField();
                itemSizeInput.setPreferredSize(new Dimension(75, 20));

                //Create Button and Action Listener
                JButton addItem = new JButton("Add Item");
                addItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //Get Input from Text Fields
                        String nameTemp = itemNameInput.getText();
                        String destTemp = itemDestinationInput.getText();
                        String weightTemp = itemWeightInput.getText();
                        String sizeTemp = itemSizeInput.getText();

                        //Validate Input
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

                            //If data is valid, create new Item
                            Item item = new Item(itemInt, nameTemp, destInt, weightInt, sizeInt);

                            //Add to Inventory and increment itemInt
                            o2a.AddItemToInventory(item);
                            itemInt++;

                            //Close Window
                            addItemWindow.dispose();
                        } catch(Exception ex) {
                            System.out.println("Invalid Data Entered. Item Not Created.");
                        }
                    }
                });

                //Outside padding for window elements
                Insets inset = new Insets(5, 5, 5, 5);

                //Use GridBagConstraints Object to position window elements and add to window
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

                //Display Window
                addItemWindow.setSize(300, 250);
                addItemWindow.setVisible(true);
            }
        });

        //6 - Generate Random Items Button
        generateItems.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Get valid int value
                int count = getIntValue("Enter Number Of Items to Generate", "", 1, false);

                if(count == CANCEL) {
                    return;
                }

                Random r = new Random();

                //Generate that number of random items, incrementing itemInt each time
                for(int i = 0; i < count; i++) {
                    Item item = new Item(itemInt, "Item" + itemInt, r.nextInt(nodeCount - 1) + 1, r.nextInt(WEIGHT_SIZE_MODIFIER - 1) + 1, r.nextInt(WEIGHT_SIZE_MODIFIER - 1) + 1);
                    itemInt++;

                    o2a.AddItemToInventory(item);
                }
            }
        });

        //7 - Load Items From File Button
        loadItemsFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadItems();
            }
        });

        //8 - Set Options Button
        setOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create Window
                JFrame optionWindow = new JFrame("Set Options");

                //Create Layout Objects
                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints gbc = new GridBagConstraints();
                optionWindow.setLayout(layout);

                //Create Window Labels
                JLabel optionMap = new JLabel("Save Map to File? Y/N: ");
                JLabel optionItems = new JLabel("Save MRA Inventory to File? Y/N: ");
                JLabel optionAutoStart = new JLabel("Load Map and Inventory on StartUp? Y/N: ");

                //Create Window Inputs
                JTextField optionMapInput = new JTextField();
                optionMapInput.setPreferredSize(new Dimension(75, 20));
                JTextField optionItemsInput = new JTextField();
                optionItemsInput.setPreferredSize(new Dimension(75, 20));
                JTextField optionAutoStartInput = new JTextField();
                optionAutoStartInput.setPreferredSize(new Dimension(75, 20));

                JButton optionButton = new JButton("Set Options");
                optionButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //Get Inputs
                        String mapInput = optionMapInput.getText();
                        String itemInput = optionItemsInput.getText();
                        String autoStartInput = optionAutoStartInput.getText();

                        //Validate Inputs
                        try{
                            if(mapInput.isEmpty() || itemInput.isEmpty() || autoStartInput.isEmpty()) {
                                throw new Exception();
                            }

                            //Call functions based on input
                            if(mapInput.equals("Y")) {
                                saveMap();
                            }

                            if(itemInput.equals("Y")) {
                                saveItems();
                            }

                            setPreferences(autoStartInput.equals("Y"));

                            System.out.println("Options Set.");

                            optionWindow.dispose();
                        } catch(Exception ex) {
                            System.out.println("Invalid Input. Options Not Set.");
                        }
                    }
                });

                //Outside padding for window objects
                Insets inset = new Insets(5, 5, 5, 5);

                //Position Window Elements using GridBagConstraints
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.insets = inset;
                optionWindow.add(optionMap, gbc);

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.insets = inset;
                optionWindow.add(optionItems, gbc);

                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.insets = inset;
                optionWindow.add(optionAutoStart, gbc);

                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.insets = inset;
                optionWindow.add(optionMapInput, gbc);

                gbc.gridx = 1;
                gbc.gridy = 1;
                gbc.insets = inset;
                optionWindow.add(optionItemsInput, gbc);

                gbc.gridx = 1;
                gbc.gridy = 2;
                gbc.insets = inset;
                optionWindow.add(optionAutoStartInput, gbc);

                gbc.gridx = 1;
                gbc.gridy = 3;
                gbc.insets = inset;
                optionWindow.add(optionButton, gbc);

                //Display Window
                optionWindow.setSize(400, 250);
                optionWindow.setVisible(true);
            }
        });

        //Create Redirected Console Output Window Elements
        JScrollPane appOutputScroll = new JScrollPane(appOutput);
        JScrollPane agentOutputScroll = new JScrollPane(agentOutput);
        JScrollPane snifferOutputScroll = new JScrollPane(snifferOutput);

        //Create App Window
        JFrame appFrame = new JFrame("Vehicle Routing App");

        //Create Layout Objects
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        appFrame.setLayout(layout);

        //Use Layout Objects to position window elements
        JLabel heading = new JLabel("<html><h1><strong><i>Vehicle Routing</i></strong></h1><hr></html>");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        appFrame.add(heading, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 0, 0);
        MasterAgentButton.setPreferredSize(new Dimension(150, 45));
        appFrame.add(MasterAgentButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 0, 0);
        listMasterInventory.setPreferredSize(new Dimension(150, 45));
        appFrame.add(listMasterInventory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 0, 0);
        DeliveryAgentButton.setPreferredSize(new Dimension(150, 45));
        appFrame.add(DeliveryAgentButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 0, 0);
        CheckAgentButton.setPreferredSize(new Dimension(150, 45));
        appFrame.add(CheckAgentButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 0, 0);
        addItem.setPreferredSize(new Dimension(150, 45));
        appFrame.add(addItem, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 0, 0);
        generateItems.setPreferredSize(new Dimension(150, 45));
        appFrame.add(generateItems, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 0, 0);
        loadItemsFile.setPreferredSize(new Dimension(150, 45));
        appFrame.add(loadItemsFile, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 0, 0);
        setOptions.setPreferredSize(new Dimension(150, 45));
        appFrame.add(setOptions, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.insets = new Insets(10, 10, 0, 10);
        appOutputScroll.setPreferredSize(new Dimension(350, 150));
        appFrame.add(appOutputScroll, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridheight = 2;
        gbc.insets = new Insets(10, 10, 0, 10);
        agentOutputScroll.setPreferredSize(new Dimension(350, 150));
        appFrame.add(agentOutputScroll, gbc);

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridheight = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        snifferOutputScroll.setPreferredSize(new Dimension(350, 100));
        appFrame.add(snifferOutputScroll, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridheight = 6;
        gbc.insets = new Insets(10, 10, 10, 10);
        graphVis = new GraphVis(o2a.getMap());
        appFrame.add(graphVis, gbc);

        //Display Window
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

    //User Input Methods
    //Creates an input window that requires an input from the user, using the message + additionalMessage and initialValue parameters
    //
    //Input is validated, if valid
    //      -The value is returned
    //      -Otherwise return another call of this method
    //
    //If the required parameter is true
    //      -Cancelling the dialog returns another call of this method
    //      -Otherwise returns the CANCEL constant
    private int getIntValue(String message, String additionalMessage, int initialValue, boolean required) {
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
    private int getDaIndex(String message) {
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

    //File IO Methods
    //Converts 2D array mapData to json and saves it to MAP_DATA_FILE
    private void saveMap() {
        Gson gson = new Gson();
        String graph = gson.toJson(o2a.getMap());

        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(MAP_DATA_FILE));

            out.write(graph);
        } catch(IOException ex) {
            System.out.println("Writing Items to File Caused IOException.");
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            } catch(IOException ex) {
                System.out.println("Closing FileWriter Caused Exception.");
            }
        }
    }

    //Deserializes json read from MAP_DATA_FILE
    //Based on Deserializes map, calls o2a.setMap and sets value of nodeCount
    private void loadMap() {
        BufferedReader in = null;

        try{
            in = new BufferedReader(new FileReader(MAP_DATA_FILE));

            String line;
            if((line = in.readLine()) != null) {
                Gson gson = new Gson();
                try {
                    int[][] map = gson.fromJson(line, int[][].class);

                    o2a.setMap(map);

                    nodeCount = map.length;

                } catch(JsonSyntaxException ex) {
                    System.out.println("Read Map Data Was Invalid. Generating Default Map");
                    generateDefaultMap();
                }
            }

        } catch(FileNotFoundException ex) {
            System.out.println("Map Data File is Missing.");
            generateDefaultMap();
        } catch(IOException ex) {
            System.out.println("Reading Map Data Caused IOException.");
            generateDefaultMap();
        } catch(Exception ex) {
            System.out.println("Reading Map Data Caused Exception.");
            generateDefaultMap();
        } finally {
            try{
                if(in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                System.out.println("Closing File Reader Caused Exception.");
            }
        }
    }

    //Tells MasterRouting Agent to generate a map with small, set values
    //Sets value of nodeCount
    //Called when file loading of map causes exception
    private void generateDefaultMap() {
        System.out.println("Generating Default Map");
        o2a.GenerateMap(5, 1, 5, 5, 15);
        nodeCount = 5;
    }

    //Saves Items in MasterRoutingAgent.masterInventory to ITEM_DATA_FILE
    //Each item is saved on a new line in the format:
    //Item.getName + FILE_DELIMITER + item.getDestination + FILE_DELIMITER + item.getWeight + FILE_DELIMITER + item.getSize
    //eg. Item1;2;5;3
    private void saveItems() {
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(ITEM_DATA_FILE));
            ArrayList<Item> items = o2a.getItems();

            if(items.size() > 0) {
                for(int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);
                    out.write(item.getName() + FILE_DELIMITER + item.getDestination() + FILE_DELIMITER + item.getWeight() + FILE_DELIMITER + item.getSize());
                    if(i != items.size() - 1) {
                        out.newLine();
                    }
                }
            } else {
                System.out.println("No Items to Save");
            }

        } catch(IOException ex) {
            System.out.println("Writing Items to File Caused IOException");
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            } catch(IOException ex) {
                System.out.println("Closing FileWriter Caused Exception");
            }
        }
    }

    //Reads ITEM_DATA_FILE line by line
    //For each line
    //      -Splits using FILE_DELIMITER
    //      -Checks String has been split into appropriate substrings
    //      -Validates substrings
    //      -Creates an Item and adds to MasterRoutingAgent.masterInventory
    //      -Increments itemCount
    private void loadItems() {
        BufferedReader in = null;

        try{
            in = new BufferedReader(new FileReader(ITEM_DATA_FILE));

            String line;
            String[] splitLine;
            Random r = new Random();
            while((line = in.readLine()) != null) {
                splitLine = line.split(FILE_DELIMITER);

                //System.out.println(line + " " + splitLine.length);

                if(splitLine.length == 4) {
                    try{
                        int dest = Integer.parseInt(splitLine[1]);
                        int weight = Integer.parseInt(splitLine[2]);
                        int size = Integer.parseInt(splitLine[3]);

                        if(dest >= nodeCount) {
                            System.out.println("Read Item has an invalid destination, based on the current map. Assigning a Random Destination");
                            dest = r.nextInt(nodeCount - 1) + 1;
                        }

                        Item item = new Item(itemInt, splitLine[0], dest, weight, size);

                        o2a.AddItemToInventory(item);

                        itemInt++;

                    } catch(NumberFormatException ex) {
                        System.out.println("Read Item had Invalid Number. Skipping Item");
                    }
                }
                else {
                    System.out.println("Read Item was not valid. Skipping Item");
                }
            }

        } catch(FileNotFoundException ex) {
            System.out.println("Item Data File is Missing. No Items Loaded");
        } catch(IOException ex) {
            System.out.println("Reading Item Data Caused IOException. No Items Loaded");
        } catch(Exception ex) {
            System.out.println("Reading Item Data Caused Exception. No Items Loaded");
        } finally {
            try{
                if(in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                System.out.println("Closing File Reader Caused Exception.");
            }
        }
    }

    //Creates OPTION_FILE with default preferences
    //Called when OPTION_FILE does not exist, or contains invalid data
    private void defaultPreferences() {
        System.out.println("Creating Default Options File");
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(OPTION_FILE));

            out.write("0");
        } catch(IOException ex) {
            System.out.println("Writing Default Preferences to File Caused IOException");
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            } catch(IOException ex) {
                System.out.println("Closing FileWriter Caused Exception");
            }
        }
    }

    //Writes either 0 or 1 to OPTION_FILE
    //      -1 if autoLoad is true
    //      -0 otherwise
    private void setPreferences(boolean autoLoad) {
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(OPTION_FILE));

            if(autoLoad) {
                out.write("1");
            }
            else {
                out.write("0");
            }
        } catch(IOException ex) {
            System.out.println("Writing Preferences to File Caused IOException");
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            } catch(IOException ex) {
                System.out.println("Closing FileWriter Caused Exception");
            }
        }
    }

    //Reads from OPTION_FILE
    //returns if OPTION_FILE data == 1
    //calls defaultPreferences and returns false if OPTION_FILE is missing or invalid
    private boolean getPreferences() {
        BufferedReader in = null;

        boolean result = false;

        try{
            in = new BufferedReader(new FileReader(OPTION_FILE));

            String line = in.readLine();

            if(line.equals("1")) {
                result = true;
            }

        } catch(FileNotFoundException ex) {
            System.out.println("Item Data File is Missing. No Items Loaded");
            defaultPreferences();
        } catch(IOException ex) {
            System.out.println("Reading Item Data Caused IOException. No Items Loaded");
            defaultPreferences();
        } catch(Exception ex) {
            System.out.println("Reading Item Data Caused Exception. No Items Loaded");
            defaultPreferences();
        } finally {
            try{
                if(in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                System.out.println("Closing File Reader Caused Exception.");
            }
        }
        return result;
    }

    public class TextUpdater {
        //Previous Targeted JTextArea, to handle the way System.out.println() handles creating a new line
        JTextArea prevTarget = appOutput;

        //Parameters
        //
        //String text: The text to be appended to the JTextArea
        //
        //Searches text  for certain substrings
        //Sets targeted JTextArea if text contains certain substrings
        private void updateText(final String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JTextArea target = appOutput;

                    if(text.contains(MRA_TEXT)) {
                        target = appOutput;
                    }

                    if(text.contains(DA_TEXT)) {
                        target = agentOutput;
                    }

                    if(text.contains(MRA_TEXT) && text.contains(DA_TEXT)) {
                        target = appOutput;
                    }

                    if(text.contains(Message.MESSAGE)) {
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

    //PaintPanel Extension class, used to draw map and DA paths to GUI
    public class GraphVis extends JPanel {

        private static final int WINDOW_SIZE = 500;
        private static final int NODE_SIZE = 16;
        private static final int TEXT_OFFSET_X = -3;
        private static final int TEXT_OFFSET_Y = 5;
        private static final int DEPOT_OFFSET = 20;

        private final Color DEPOT_COLOR = Color.RED;
        private final Color NODE_COLOR = Color.GREEN;
        private final Color TEXT_COLOR = Color.BLACK;
        private final Color WEIGHT_COLOR = Color.DARK_GRAY;
        private final Color EDGE_COLOR = Color.ORANGE;
        private final Color PATH_COLOR = Color.BLUE;

        private ArrayList<MapNode> nodes = new ArrayList<>();
        private int[][] map;

        Path pathToDraw;

        public GraphVis(int[][] mapData) {
            setBorder(BorderFactory.createLineBorder(Color.black));

            map = mapData;

            for(int i = 0; i < mapData.length; i++) {
                nodes.add(new MapNode(i, 0, 0));
            }

            //Set Node Positions
            Random r = new Random();

            //Set Node Positions
            nodes.get(0).setX(WINDOW_SIZE / 2);
            nodes.get(0).setY(WINDOW_SIZE / 2);

            //Set Node Positions
            for(int i = 1; i < nodes.size(); i++) {
                MapNode node = nodes.get(i);

                node.setX(r.nextInt(WINDOW_SIZE - (NODE_SIZE * 2)) + NODE_SIZE);
                if(node.getX() > ((WINDOW_SIZE / 2) - DEPOT_OFFSET) && node.getX() < ((WINDOW_SIZE / 2) + DEPOT_OFFSET)) {
                    if(r.nextInt(2) != 0) {
                        node.setX((WINDOW_SIZE / 2) + DEPOT_OFFSET);
                    }
                    else {
                        node.setX((WINDOW_SIZE / 2) - DEPOT_OFFSET);
                    }
                }

                node.setY(r.nextInt(WINDOW_SIZE - (NODE_SIZE * 2)) + NODE_SIZE);
                if(node.getY() > ((WINDOW_SIZE / 2) - DEPOT_OFFSET) && node.getY() < ((WINDOW_SIZE / 2) + DEPOT_OFFSET)) {
                    if(r.nextInt(2) != 0) {
                        node.setY((WINDOW_SIZE / 2) + DEPOT_OFFSET);
                    }
                    else {
                        node.setY((WINDOW_SIZE / 2) - DEPOT_OFFSET);
                    }
                }
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            //Draw Edges
            for(int i = 0; i < nodes.size(); i++) {
                drawEdge(g, i);
            }

            //Draw Nodes
            for(int i = 1; i < nodes.size(); i++) {
                drawNode(g, i);
            }

            drawDepot(g);

            if(pathToDraw != null) {
                drawPath(g);
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(WINDOW_SIZE, WINDOW_SIZE);
        }

        public void drawDepot(Graphics g) {
            g.setColor(DEPOT_COLOR);
            g.fillOval(nodes.get(0).getX() - (NODE_SIZE / 2), nodes.get(0).getY() - (NODE_SIZE / 2), NODE_SIZE, NODE_SIZE);

            g.setColor(TEXT_COLOR);
            g.drawString(String.valueOf(nodes.get(0).getIndex()), nodes.get(0).getX() + TEXT_OFFSET_X, nodes.get(0).getY() + TEXT_OFFSET_Y);
        }

        public void drawNode(Graphics g, int index) {
            g.setColor(NODE_COLOR);
            g.fillOval(nodes.get(index).getX() - (NODE_SIZE / 2), nodes.get(index).getY() - (NODE_SIZE / 2), NODE_SIZE, NODE_SIZE);

            g.setColor(TEXT_COLOR);
            g.drawString(String.valueOf(nodes.get(index).getIndex()), nodes.get(index).getX() + TEXT_OFFSET_X, nodes.get(index).getY() + TEXT_OFFSET_Y);
        }

        public void drawEdge(Graphics g, int index) {
            for(int i = 0; i < nodes.size(); i++) {
                if(i != index) {
                    if(map[index][i] != 0) {
                        g.setColor(EDGE_COLOR);
                        g.drawLine(nodes.get(index).getX(), nodes.get(index).getY(), nodes.get(i).getX(), nodes.get(i).getY());

                        int x = (nodes.get(index).getX() + nodes.get(i).getX()) / 2;
                        int y = (nodes.get(index).getY() + nodes.get(i).getY()) / 2;

                        g.setColor(WEIGHT_COLOR);
                        g.drawString(String.valueOf(map[index][i]), x + TEXT_OFFSET_X, y + TEXT_OFFSET_Y);
                    }
                }
            }
        }

        public void drawPath(Graphics g) {
            int[] locations = pathToDraw.getLocations();

            g.setColor(PATH_COLOR);

            g.drawLine(nodes.get(0).getX(), nodes.get(0).getY(), nodes.get(locations[0]).getX(), nodes.get(locations[0]).getY());

            for(int i = 0; i < locations.length - 1; i++) {
                g.drawLine(nodes.get(locations[i]).getX(), nodes.get(locations[i]).getY(), nodes.get(locations[i + 1]).getX(), nodes.get(locations[i + 1]).getY());
            }
        }

        public void setPathToDraw(Path path) {
            pathToDraw = path;
        }
    }
}
