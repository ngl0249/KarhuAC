package me.liwk.karhu.replay.data.entity;

import me.liwk.karhu.replay.packet.PacketData;

public class InitialEntityData implements PacketData {
    private final int entityId;
    private final String entityType;
    private final double x, y, z;
    private final float yaw, pitch;
    private final String customName;

    public InitialEntityData(int entityId, String entityType, double x, double y, double z,
                             float yaw, float pitch, String customName) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
        this.customName = customName;
    }

    public int getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public String getCustomName() { return customName; }
}
