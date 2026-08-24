package me.liwk.karhu.replay.data.world;

import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import me.liwk.karhu.replay.packet.PacketData;

// New Packet Data Classes for Enhanced Recording
public class ChunkData implements PacketData {
    private final int x, z;
    private final Column column;

    public ChunkData(int x, int z, Column column) {
        this.x = x; this.z = z; this.column = column;
    }

    public int getX() { return x; }
    public int getZ() { return z; }
    public Column getColumn() { return column; }
}
