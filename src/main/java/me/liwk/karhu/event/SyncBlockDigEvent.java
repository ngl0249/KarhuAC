package me.liwk.karhu.event;


import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter
public class SyncBlockDigEvent extends Event {

    private final Block block;
    private final Location playerLoc;

    public SyncBlockDigEvent(Block blockPos, Location playerLoc) {
        this.block = blockPos;
        this.playerLoc = playerLoc;
    }
}
