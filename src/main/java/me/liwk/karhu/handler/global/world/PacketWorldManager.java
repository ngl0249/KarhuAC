package me.liwk.karhu.handler.global.world;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.ConcurrentHashMap;

public class PacketWorldManager {

    private final KarhuPlayer data;

    private final ConcurrentHashMap<World, Long2ObjectMap<Chunk>> loadedChunks = new ConcurrentHashMap<>();

    public PacketWorldManager(KarhuPlayer data) {
        this.data = data;
    }


    public void handleChunkData(WrapperPlayServerChunkData wrapper) {

    }

    public void handleChunkDataBulk(WrapperPlayServerChunkDataBulk wrapper) {

    }

    public void handleChunkUnload(WrapperPlayServerUnloadChunk wrapper) {

    }

    public void handleBlockChange(WrapperPlayServerBlockChange wrapper) {

    }

    public void handleMultiBlockChange(WrapperPlayServerMultiBlockChange wrapper) {

    }

    public void handleBlockPlacement(WrapperPlayClientPlayerBlockPlacement wrapper) {

    }

    public void handleBlockBreak(WrapperPlayClientPlayerDigging wrapper) {

    }
}
