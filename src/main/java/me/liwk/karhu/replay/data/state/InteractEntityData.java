package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

public class InteractEntityData implements PacketData {
    private final int entityId;
    private final String action;

    public InteractEntityData(int entityId, String action) {
        this.entityId = entityId; this.action = action;
    }

    public int getEntityId() { return entityId; }
    public String getAction() { return action; }
}
