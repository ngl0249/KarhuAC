package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

// Additional packet data classes (keeping existing ones)
public class PlayerPositionData implements PacketData {
    private final double x, y, z;
    private final boolean onGround;

    public PlayerPositionData(double x, double y, double z, boolean onGround) {
        this.x = x; this.y = y; this.z = z; this.onGround = onGround;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public boolean isOnGround() { return onGround; }
}
