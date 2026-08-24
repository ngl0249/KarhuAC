package me.liwk.karhu.replay.data.world;

import me.liwk.karhu.replay.data.entity.TileEntityData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkDataSnapshot {
    private final int x, z;
    private final Map<String, String> blocks = new HashMap<>(); // "x,y,z" -> "material:blockdata"
    private final Map<String, String> biomes = new HashMap<>(); // "x,z" -> "biome"
    private final List<TileEntityData> tileEntities = new ArrayList<>();
    private final byte[] lightData = new byte[0]; // Simplified - would contain actual light data

    public ChunkDataSnapshot(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public void setBlock(int x, int y, int z, String material, String blockData) {
        blocks.put(x + "," + y + "," + z, material + ":" + blockData);
    }

    public void setBiome(int x, int z, String biome) {
        biomes.put(x + "," + z, biome);
    }

    public void addTileEntity(int x, int y, int z, String type, Map<String, Object> data) {
        tileEntities.add(new TileEntityData(x, y, z, type, data));
    }

    // Getters
    public int getX() { return x; }
    public int getZ() { return z; }
    public Map<String, String> getBlocks() { return blocks; }
    public Map<String, String> getBiomes() { return biomes; }
    public List<TileEntityData> getTileEntities() { return tileEntities; }
    public byte[] getLightData() { return lightData; }

}
