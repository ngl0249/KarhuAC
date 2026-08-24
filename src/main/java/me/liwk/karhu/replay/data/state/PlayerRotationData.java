package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

public class PlayerRotationData implements PacketData {
    private final float yaw, pitch;
    private final boolean onGround;

    public PlayerRotationData(float yaw, float pitch, boolean onGround) {
        this.yaw = yaw; this.pitch = pitch; this.onGround = onGround;
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }
}
