package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

public class VelocityData implements PacketData {
    private final double velX, velY, velZ;

    public VelocityData(double velX, double velY, double velZ) {
        this.velX = velX; this.velY = velY; this.velZ = velZ;
    }

    public double getVelX() { return velX; }
    public double getVelY() { return velY; }
    public double getVelZ() { return velZ; }
}
