package me.liwk.karhu.replay.view;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.replay.data.entity.InitialEntityData;
import me.liwk.karhu.replay.data.state.InitialInventoryData;
import me.liwk.karhu.replay.data.state.InitialPlayerStateData;
import me.liwk.karhu.replay.data.world.ChunkDataSnapshot;
import me.liwk.karhu.replay.data.world.InitialChunkData;
import me.liwk.karhu.replay.packet.PacketDirection;
import me.liwk.karhu.replay.packet.PacketType;
import me.liwk.karhu.replay.packet.ReplayPacket;
import me.liwk.karhu.replay.session.ReplaySession;
import me.liwk.karhu.replay.storage.ReplayInfo;
import me.liwk.karhu.replay.storage.ReplayStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReplayManager {

    private final Karhu plugin;
    private final Map<UUID, ReplaySession> activeSessions;
    private final ReplayStorage storage;
    private final ScheduledExecutorService executor;

    private final int MAX_RECORDING_TIME = 300; // 5 minutes
    private final int MAX_PACKETS_PER_SESSION = 100000; // Increased for chunk data

    public ReplayManager(Karhu plugin) {
        this.plugin = plugin;
        this.activeSessions = new ConcurrentHashMap<>();
        this.storage = new ReplayStorage(plugin);
        this.executor = Executors.newScheduledThreadPool(2);

        // Clean up old sessions every minute
        executor.scheduleAtFixedRate(this::cleanupOldSessions, 60, 60, TimeUnit.SECONDS);
    }

    public void startRecording(Player player, String reason) {
        UUID playerId = player.getUniqueId();

        // Stop existing recording if any
        stopRecording(playerId);

        ReplaySession session = new ReplaySession(
                playerId,
                player.getName(),
                System.currentTimeMillis(),
                reason,
                MAX_PACKETS_PER_SESSION
        );

        // Capture initial world state
        captureInitialWorldState(player, session);

        activeSessions.put(playerId, session);
        plugin.getLogger().info("Started recording replay for " + player.getName() + " - Reason: " + reason);
    }

    private void captureInitialWorldState(Player player, ReplaySession session) {
        long timestamp = System.currentTimeMillis();

        // Capture initial player state
        Location loc = player.getLocation();
        session.addPacket(new ReplayPacket(
                timestamp,
                PacketDirection.INITIAL_STATE,
                PacketType.INITIAL_PLAYER_STATE,
                new InitialPlayerStateData(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(),
                        player.getHealth(), player.getFoodLevel(), player.getGameMode().name())
        ));

        // Capture loaded chunks around player
        captureLoadedChunks(player, session, timestamp);

        // Capture nearby entities
        captureNearbyEntities(player, session, timestamp);

        // Capture initial inventory
        capturePlayerInventory(player, session, timestamp);
    }

    private void captureLoadedChunks(Player player, ReplaySession session, long timestamp) {
        Location playerLoc = player.getLocation();
        int playerChunkX = playerLoc.getBlockX() >> 4;
        int playerChunkZ = playerLoc.getBlockZ() >> 4;
        int viewDistance = player.getWorld().getViewDistance();

        // Capture chunks in view distance by sending them as packets to capture the data
        for (int x = playerChunkX - viewDistance; x <= playerChunkX + viewDistance; x++) {
            for (int z = playerChunkZ - viewDistance; z <= playerChunkZ + viewDistance; z++) {
                if (player.getWorld().isChunkLoaded(x, z)) {
                    // Create a packet listener to capture the chunk data when we request it
                    captureChunkDataPacket(player, x, z, session, timestamp);
                }
            }
        }
    }

    private void captureChunkDataPacket(Player player, int chunkX, int chunkZ, ReplaySession session, long timestamp) {
        // Force the server to send a chunk packet by temporarily creating a packet intercept
        // We'll create a temporary listener to capture the chunk data

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Get the chunk from the world
                org.bukkit.Chunk chunk = player.getWorld().getChunkAt(chunkX, chunkZ);

                // Create chunk data packet using NMS or reflection to get the actual packet data
                // For now, we'll create a simplified version and store essential data

                // Create our own chunk data representation
                ChunkDataSnapshot chunkSnapshot = createChunkSnapshot(chunk);

                session.addPacket(new ReplayPacket(
                        timestamp,
                        PacketDirection.INITIAL_STATE,
                        PacketType.INITIAL_CHUNK,
                        new InitialChunkData(chunkX, chunkZ, chunkSnapshot)
                ));

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to capture chunk data for " + chunkX + "," + chunkZ + ": " + e.getMessage());
            }
        });
    }

    private ChunkDataSnapshot createChunkSnapshot(org.bukkit.Chunk chunk) {
        ChunkDataSnapshot snapshot = new ChunkDataSnapshot(chunk.getX(), chunk.getZ());

        // Capture block data for the chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                    org.bukkit.block.Block block = chunk.getBlock(x, y, z);
                    if (block.getType() != org.bukkit.Material.AIR) {
                        snapshot.setBlock(x, y, z, block.getType().name(), block.getBlockData().getAsString());
                    }
                }

                // Capture biome data
                snapshot.setBiome(x, z, chunk.getBlock(x, 64, z).getBiome().name());
            }
        }

        // Capture tile entities (chests, signs, etc.)
        for (org.bukkit.block.BlockState tileEntity : chunk.getTileEntities()) {
            snapshot.addTileEntity(tileEntity.getX() & 15, tileEntity.getY(), tileEntity.getZ() & 15,
                    tileEntity.getType().name(), serializeTileEntityData(tileEntity));
        }

        return snapshot;
    }

    private Map<String, Object> serializeTileEntityData(org.bukkit.block.BlockState tileEntity) {
        Map<String, Object> data = new HashMap<>();

        // Handle different tile entity types
        if (tileEntity instanceof org.bukkit.block.Sign) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) tileEntity;
            data.put("lines", sign.getLines());
        } else if (tileEntity instanceof org.bukkit.block.Chest) {
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) tileEntity;
            data.put("inventory", serializeInventory(chest.getInventory().getContents()));
        } else if (tileEntity instanceof org.bukkit.block.Furnace) {
            org.bukkit.block.Furnace furnace = (org.bukkit.block.Furnace) tileEntity;
            data.put("inventory", serializeInventory(furnace.getInventory().getContents()));
            data.put("burnTime", furnace.getBurnTime());
            data.put("cookTime", furnace.getCookTime());
        }
        // Add more tile entity types as needed

        return data;
    }

    private void captureNearbyEntities(Player player, ReplaySession session, long timestamp) {
        Location playerLoc = player.getLocation();
        double captureRadius = 64.0; // Capture entities within 64 blocks

        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(playerLoc, captureRadius, captureRadius, captureRadius)) {
            if (entity.equals(player)) continue; // Skip the player themselves

            Location entityLoc = entity.getLocation();
            session.addPacket(new ReplayPacket(
                    timestamp,
                    PacketDirection.INITIAL_STATE,
                    PacketType.INITIAL_ENTITY,
                    new InitialEntityData(
                            entity.getEntityId(),
                            entity.getType().name(),
                            entityLoc.getX(), entityLoc.getY(), entityLoc.getZ(),
                            entityLoc.getYaw(), entityLoc.getPitch(),
                            entity.getCustomName()
                    )
            ));
        }
    }

    private void capturePlayerInventory(Player player, ReplaySession session, long timestamp) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        session.addPacket(new ReplayPacket(
                timestamp,
                PacketDirection.INITIAL_STATE,
                PacketType.INITIAL_INVENTORY,
                new InitialInventoryData(
                        serializeInventory(inv.getContents()),
                        serializeInventory(inv.getArmorContents()),
                        inv.getHeldItemSlot()
                )
        ));
    }

    private Map<Integer, String> serializeInventory(org.bukkit.inventory.ItemStack[] items) {
        Map<Integer, String> serialized = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getType() != org.bukkit.Material.AIR) {
                serialized.put(i, items[i].getType().name() + ":" + items[i].getAmount());
            }
        }
        return serialized;
    }

    public void stopRecording(UUID playerId) {
        ReplaySession session = activeSessions.remove(playerId);
        if (session != null) {
            // Save to storage asynchronously
            executor.submit(() -> {
                try {
                    storage.saveReplay(session);
                    plugin.getLogger().info("Saved replay for " + session.getPlayerName() +
                            " (" + session.getPackets().size() + " packets, " + session.getChunks().size() + " chunks)");
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to save replay: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    public void recordPacket(Player player, ReplayPacket packet) {
        ReplaySession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.addPacket(packet);
        }
    }

    public boolean isRecording(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    public List<ReplayInfo> getAvailableReplays() {
        return storage.getAvailableReplays();
    }

    public ReplaySession loadReplay(String replayId) {
        return storage.loadReplay(replayId);
    }

    public void deleteReplay(String replayId) {
        storage.deleteReplay(replayId);
    }

    private void cleanupOldSessions() {
        long currentTime = System.currentTimeMillis();
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, ReplaySession> entry : activeSessions.entrySet()) {
            ReplaySession session = entry.getValue();

            // Remove sessions older than MAX_RECORDING_TIME
            if (currentTime - session.getStartTime() > MAX_RECORDING_TIME * 1000L) {
                toRemove.add(entry.getKey());
            }
        }

        for (UUID playerId : toRemove) {
            stopRecording(playerId);
        }
    }

    public void shutdown() {
        // Save all active sessions
        for (UUID playerId : new ArrayList<>(activeSessions.keySet())) {
            stopRecording(playerId);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}