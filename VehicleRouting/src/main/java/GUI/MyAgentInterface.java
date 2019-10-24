package GUI;

import Item.Item;

import java.io.OutputStream;

public interface MyAgentInterface {
    void StartMasterAgent();
    boolean AddItemToInventory(Item i);
    void GenerateMap(int v, int dMin, int dMax, int eMin, int eMax);
    void OverwriteOutput(OutputStream out);
}