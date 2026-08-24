package me.liwk.karhu.replay.data.entity;

import me.liwk.karhu.replay.packet.PacketData;

public class SpawnEntityData implements PacketData {
    private final int entityId;
    private final String entityType;
    private final double x, y, z;

    public SpawnEntityData(int entityId, String entityType, double x, double y, double z) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.x = x; this.y = y; this.z = z;
    }

    public int getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
