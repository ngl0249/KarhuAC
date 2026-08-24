package me.liwk.karhu.replay.session;

import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import me.liwk.karhu.replay.data.world.ChunkData;
import me.liwk.karhu.replay.packet.ReplayPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReplaySession {
    private final UUID playerId;
    private final String playerName;
    private final long startTime;
    private final String reason;
    private final List<ReplayPacket> packets;
    private final Map<String, Column> chunks; // chunkKey -> Column
    private final int maxPackets;

    public ReplaySession(UUID playerId, String playerName, long startTime, String reason, int maxPackets) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.startTime = startTime;
        this.reason = reason;
        this.maxPackets = maxPackets;
        this.packets = Collections.synchronizedList(new ArrayList<>());
        this.chunks = new ConcurrentHashMap<>();
    }

    public void addPacket(ReplayPacket packet) {
        if (packets.size() < maxPackets) {
            packets.add(packet);

            // Store chunk data if applicable
            if (packet.getData() instanceof ChunkData) {
                ChunkData chunkData = (ChunkData) packet.getData();
                String chunkKey = chunkData.getX() + "," + chunkData.getZ();
                chunks.put(chunkKey, chunkData.getColumn());
            }
        }
    }

    public String getReplayId() {
        return playerId.toString() + "_" + startTime;
    }

    public Set<String> getLoadedChunks() {
        return new HashSet<>(chunks.keySet());
    }

    public Column getChunk(int x, int z) {
        return chunks.get(x + "," + z);
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public long getStartTime() { return startTime; }
    public String getReason() { return reason; }
    public List<ReplayPacket> getPackets() { return new ArrayList<>(packets); }
    public Map<String, Column> getChunks() { return new HashMap<>(chunks); }
    public long getDuration() {
        if (packets.isEmpty()) return 0;
        return packets.get(packets.size() - 1).getTimestamp() - startTime;
    }
}

