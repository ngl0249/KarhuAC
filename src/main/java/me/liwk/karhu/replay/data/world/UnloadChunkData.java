package me.liwk.karhu.replay.data.world;

import me.liwk.karhu.replay.packet.PacketData;

public class UnloadChunkData implements PacketData {
    private final int x, z;

    public UnloadChunkData(int x, int z) {
        this.x = x; this.z = z;
    }

    public int getX() { return x; }
    public int getZ() { return z; }
}
