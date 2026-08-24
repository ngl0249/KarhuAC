package me.liwk.karhu.manager;

import lombok.Getter;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.BanWaveX;
import me.liwk.karhu.util.bungee.BungeeAPI;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class WaveManager {
    // Use volatile for thread visibility
    private volatile boolean runningBanwave = false;

    // Synchronization object
    private final Object banwaveLock = new Object();

    private final Set<String> playersToBan = Collections.synchronizedSet(new HashSet<>());
    private final AtomicInteger bans = new AtomicInteger(0);

    // Tracking start time and timeout
    private final AtomicLong banwaveStartTime = new AtomicLong(0);
    private static final long BASE_TIMEOUT_MILLISECONDS = 5 * 60 * 1000; // 5 minutes base timeout
    private static final long PLAYER_TIMEOUT_MULTIPLIER = 1000; // 1 second per player
    private static final long TIMEOUT_BUFFER = 60 * 1000; // 1 minute buffer

    /**
     * Check if a banwave is currently running
     * @return true if banwave is in progress
     */
    public boolean isRunningBanwave() {
        return runningBanwave;
    }

    /**
     * Attempt to start a banwave with thread-safe synchronization
     * @return true if banwave was successfully started, false if already running
     */
    public boolean startBanwave() {
        synchronized (banwaveLock) {
            // Check if already running
            if (runningBanwave) {
                return false;
            }

            // Mark banwave as running
            runningBanwave = true;
            banwaveStartTime.set(System.currentTimeMillis());

            // Import players if list is empty

            importFromDb();

            // No players to ban
            if (playersToBan.isEmpty()) {
                completeBanWave();
                return false;
            }

            // Start banwave processing
            Tasker.taskAsync(this::processBanwaveWithTimeout);
            return true;
        }
    }

    private void processBanwaveWithTimeout() {
        try {
            // Process players
            processNextPlayerInBanwave();

            // Check for timeout
            long currentTime = System.currentTimeMillis();
            long startTime = banwaveStartTime.get();
            long playerCount = playersToBan.size();

            long dynamicTimeout = BASE_TIMEOUT_MILLISECONDS +
                    (playerCount * PLAYER_TIMEOUT_MULTIPLIER) +
                    TIMEOUT_BUFFER;

            if (startTime > 0 && (currentTime - startTime) > dynamicTimeout) {
                Karhu.getInstance().printCool("> Banwave timed out. Resetting...");
                resetBanwave();
            }
        } catch (Exception e) {
            Karhu.getInstance().printCool("> Banwave processing error: " + e.getMessage());
            resetBanwave();
        }
    }

    private void processNextPlayerInBanwave() {
        synchronized (playersToBan) {
            // If no more players, complete the banwave
            if (playersToBan.isEmpty()) {
                completeBanWave();
                return;
            }

            // Get and remove the first player from the list
            String playerToBan = playersToBan.iterator().next();
            removeFromWave(playerToBan);

            try {
                processPlayerBan(playerToBan);
            } catch (Exception e) {
                Karhu.getInstance().printCool("> Error during banwave: " + e.getMessage());
            }

            // Schedule next iteration or completion
            Tasker.runTaskLaterAsync(() -> {
                if (!playersToBan.isEmpty()) {
                    processNextPlayerInBanwave();
                } else {
                    completeBanWave();
                }
            }, 20L);
        }
    }


    private void processPlayerBan(String uuidOrName) {
        String player = uuidOrName.contains("-") ? findName(uuidOrName) : uuidOrName;

        if (player == null) {
            Karhu.getInstance().printCool("> Could not resolve player: " + uuidOrName);
            return;
        }

        // Broadcast and ban
        if (Karhu.getInstance().getConfigManager().isBrCaught()) {
            Bukkit.broadcastMessage(
                    Karhu.getInstance().getConfigManager().getBanwaveCaught().replace("%player%", player)
            );
        } else {
            Karhu.getInstance().printCool(
                    Karhu.getInstance().getConfigManager().getBanwaveCaught().replace("%player%", player)
            );
        }

        bans.incrementAndGet();

        String punish = Karhu.getInstance().getConfigManager().getBanwavePunish()
                .replaceAll("%player%", player);

        Tasker.run(() -> {
            if (!Karhu.getInstance().getConfigManager().isBungeeCommand()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), punish);
            } else {
                BungeeAPI.sendCommand(punish);
            }
        });
    }

    private void completeBanWave() {
        synchronized (banwaveLock) {
            int banCount = bans.get();
            if (banCount > 0) {
                if (Karhu.getInstance().getConfigManager().isBrComplete()) {
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(
                            Karhu.getInstance().getConfigManager().getBanwaveComplete()
                                    .replace("%bans%", String.valueOf(banCount))
                    );
                    Bukkit.broadcastMessage("");
                } else {
                    Karhu.getInstance().printCool(
                            Karhu.getInstance().getConfigManager().getBanwaveComplete()
                                    .replace("%bans%", String.valueOf(banCount))
                    );
                }
            } else {
                Karhu.getInstance().printCool("> Banwave completed with no bans");
            }

            resetBanwave();
        }
    }

    private void resetBanwave() {
        synchronized (banwaveLock) {
            bans.set(0);
            runningBanwave = false;
            banwaveStartTime.set(0);
            playersToBan.clear();
        }
    }

    public boolean addToWave(String uuid, String check) {
        synchronized (playersToBan) {
            if (playersToBan.contains(uuid)) {
                return false;
            }

            playersToBan.add(uuid);
            BanWaveX bwRequest = new BanWaveX(uuid, check, 1, System.currentTimeMillis());
            Karhu.getStorage().addToBanWave(bwRequest);
            return true;
        }
    }

    public void removeFromWave(String uuid) {
        synchronized (playersToBan) {
            playersToBan.remove(uuid);
            Karhu.getStorage().removeFromBanWave(uuid);
        }
    }

    public void importFromDb() {
        synchronized (playersToBan) {
            //playersToBan.clear();
            playersToBan.addAll(Karhu.storage.getBanwaveList());
        }
    }

    private String findName(String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);
            Player onlinePlayer = Bukkit.getPlayer(uuid);

            if (onlinePlayer != null) {
                return onlinePlayer.getName();
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            return offlinePlayer.getName();
        } catch (IllegalArgumentException e) {
            Karhu.getInstance().printCool("> Invalid UUID format: " + uuidString);
            return null;
        }
    }
}

