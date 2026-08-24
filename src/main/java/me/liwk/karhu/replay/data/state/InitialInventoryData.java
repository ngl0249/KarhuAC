package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

import java.util.Map;

public class InitialInventoryData implements PacketData {
    private final Map<Integer, String> mainInventory;
    private final Map<Integer, String> armorInventory;
    private final int heldItemSlot;

    public InitialInventoryData(Map<Integer, String> mainInventory, Map<Integer, String> armorInventory, int heldItemSlot) {
        this.mainInventory = mainInventory;
        this.armorInventory = armorInventory;
        this.heldItemSlot = heldItemSlot;
    }

    public Map<Integer, String> getMainInventory() { return mainInventory; }
    public Map<Integer, String> getArmorInventory() { return armorInventory; }
    public int getHeldItemSlot() { return heldItemSlot; }
}
