package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

public class TeleportData implements PacketData {
    private final double x, y, z;
    private final float yaw, pitch;

    public TeleportData(double x, double y, double z, float yaw, float pitch) {
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}
