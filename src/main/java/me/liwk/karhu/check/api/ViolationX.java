package me.liwk.karhu.check.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public final class ViolationX {
    public String player;
    public String type;
    public int vl;
    public long time;
    public String data;
    public String location;
    public String world;
    public long ping;
    public double TPS;
}
