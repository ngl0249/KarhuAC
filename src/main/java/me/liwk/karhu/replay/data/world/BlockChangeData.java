package me.liwk.karhu.replay.data.world;

import me.liwk.karhu.replay.packet.PacketData;

public class BlockChangeData implements PacketData {
    private final com.github.retrooper.packetevents.protocol.world.Location position;
    private final String blockType;

    public BlockChangeData(com.github.retrooper.packetevents.protocol.world.Location position, String blockType) {
        this.position = position;
        this.blockType = blockType;
    }

    public com.github.retrooper.packetevents.protocol.world.Location getPosition() { return position; }
    public String getBlockType() { return blockType; }
}
