package me.liwk.karhu.replay.data.entity;

import me.liwk.karhu.replay.packet.PacketData;

public class DestroyEntitiesData implements PacketData {
    private final int[] entityIds;

    public DestroyEntitiesData(int[] entityIds) {
        this.entityIds = entityIds;
    }

    public int[] getEntityIds() { return entityIds; }
}
