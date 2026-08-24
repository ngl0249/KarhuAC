package me.liwk.karhu.replay.data.entity;

import java.util.Map;

public class TileEntityData {
    private final int x, y, z;
    private final String type;
    private final Map<String, Object> data;

    public TileEntityData(int x, int y, int z, String type, Map<String, Object> data) {
        this.x = x; this.y = y; this.z = z;
        this.type = type;
        this.data = data;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getType() { return type; }
    public Map<String, Object> getData() { return data; }
}