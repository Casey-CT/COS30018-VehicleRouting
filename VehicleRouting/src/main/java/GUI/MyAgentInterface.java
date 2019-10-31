package GUI;

import Item.Item;
import jade.core.AID;

import java.io.OutputStream;

public interface MyAgentInterface {
    void StartMasterAgent();
    void AddItemToInventory(Item i);
    String listItems();
    AID getAgentName();
    void GenerateMap(int v, int dMin, int dMax, int eMin, int eMax);
    void OverwriteOutput(OutputStream out);
}