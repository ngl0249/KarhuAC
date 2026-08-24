package me.liwk.karhu.replay.data.state;

import me.liwk.karhu.replay.packet.PacketData;

public class PlayerDiggingData implements PacketData {
    private final String action;
    private final com.github.retrooper.packetevents.protocol.world.Location blockPosition;
    private final String blockFace;

    public PlayerDiggingData(String action, com.github.retrooper.packetevents.protocol.world.Location blockPosition, String blockFace) {
        this.action = action;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
    }

    public String getAction() { return action; }
    public com.github.retrooper.packetevents.protocol.world.Location getBlockPosition() { return blockPosition; }
    public String getBlockFace() { return blockFace; }
}
