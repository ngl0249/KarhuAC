package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

// New packet data classes for initial state capture
public class InitialPlayerStateData implements PacketData {
    private final double x, y, z;
    private final float yaw, pitch;
    private final double health;
    private final int foodLevel;
    private final String gameMode;

    public InitialPlayerStateData(double x, double y, double z, float yaw, float pitch,
                                  double health, int foodLevel, String gameMode) {
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
        this.health = health; this.foodLevel = foodLevel;
        this.gameMode = gameMode;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public double getHealth() { return health; }
    public int getFoodLevel() { return foodLevel; }
    public String getGameMode() { return gameMode; }
}
