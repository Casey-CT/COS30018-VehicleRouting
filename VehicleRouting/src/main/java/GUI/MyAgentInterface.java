package GUI;

import GraphGeneration.GraphGen;
import Item.Item;
import jade.core.AID;

import java.io.OutputStream;
import java.util.ArrayList;

public interface MyAgentInterface {
    void StartMasterAgent();
    void AddItemToInventory(Item i);
    ArrayList<Item> getItems();
    String listItems();
    AID getAgentName();
    int[][] getMap();
    void setMap(int[][] map);
    void GenerateMap(int v, int dMin, int dMax, int eMin, int eMax);
    void OverwriteOutput(OutputStream out);
}