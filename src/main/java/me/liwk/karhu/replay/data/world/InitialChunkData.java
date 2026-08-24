package me.liwk.karhu.replay.data.world;

import me.liwk.karhu.replay.packet.PacketData;

public class InitialChunkData implements PacketData {
    private final int x, z;
    private final ChunkDataSnapshot chunkData;

    public InitialChunkData(int x, int z, ChunkDataSnapshot chunkData) {
        this.x = x;
        this.z = z;
        this.chunkData = chunkData;
    }

    public int getX() { return x; }
    public int getZ() { return z; }
    public ChunkDataSnapshot getChunkData() { return chunkData; }
}
