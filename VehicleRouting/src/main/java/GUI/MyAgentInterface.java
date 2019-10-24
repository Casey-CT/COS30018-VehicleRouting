package GUI;

import Item.Item;

public interface MyAgentInterface {
    void StartMasterAgent();
    boolean AddItemToInventory(Item i);
    void GenerateMap(int v, int dMin, int dMax, int eMin, int eMax);
}