package me.liwk.karhu.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public class CachedBlock {

    private final Material material;
    private final Location position;
    private final boolean[] water;
    private final boolean[] lava;
}
