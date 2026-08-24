package me.liwk.karhu.handler.net;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.manager.ConfigManager;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.Teleport;
import me.liwk.karhu.util.benchmark.Benchmark;
import me.liwk.karhu.util.benchmark.BenchmarkType;
import me.liwk.karhu.util.benchmark.KarhuBenchmarker;
import me.liwk.karhu.util.gui.Callback;
import me.liwk.karhu.util.task.Tasker;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Does basic packet verifications to make sure you are receiving and both replying
 * to the transaction packets sent
 */
@RequiredArgsConstructor
public final class NetHandler {

    private final KarhuPlayer data;

    public final LinkedList<TaskData> pings = new LinkedList<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Queue<KarhuTask> postPendingTask = new ConcurrentLinkedQueue<>();
    public final Set<Long> pendingKeepalive = ConcurrentHashMap.newKeySet();
    private final Set<Long> invalidKeepalive = ConcurrentHashMap.newKeySet();
    public int sentKeep, sentTransac, receivedT, pReceived;
    private boolean kicked;

    private double violations;

    private boolean canCheck;

    private final ConfigManager configManager = Karhu.getInstance().getConfigManager();

    private static final String KICK_MSG = "java.net.IOException Connection timed out: no further information";

    public void handleFlying(boolean pos) {
        /*
        Too fishy, idk what they do if they werent banned already
         */
        if(pendingKeepalive.size() > 100) {
            kicklol(true, "(ka)");
        }
    }
    public void handleKeepAlive(long id) {

        if(pendingKeepalive.contains(id)) {
            pendingKeepalive.remove(id);
            invalidKeepalive.remove(id);
        } else {
            invalidKeepalive.add(id);

            if (invalidKeepalive.size() > 80) {
                kickxd();
            }
        }
    }
    public void handleClientTransaction(short number) {
        if (number < 0) {
            // If not ready to accept, peek instead of polling
            TaskData received;
            if (!data.isReadyToAccept()) {
                received = pings.peekLast();
                if (received == null) return; // Just ignore, don't violate

                // Check if this is a stale transaction from previous server
                if (number != received.getId()) {
                    return; // Ignore mismatched transactions during server switch
                }
                // If it matches, we can safely poll it now
                pings.pollLast();
            } else {
                received = pings.pollLast();
            }

            if (received == null) {
                if (++this.violations > 4) {
                    kicklol(false, "(null)");
                }
                return;
            }

            if (number != received.getId() && data.isReadyToAccept()) {
                if (++this.violations > 4) {
                    handleInvalidTransaction(number, (short) received.getId());
                }
                return;
            }

            // Process normally...
            long start = System.nanoTime();
            received.consumeTask();
            long end = System.nanoTime();

            Benchmark profileData = KarhuBenchmarker.getProfileData(BenchmarkType.TRANSACTION_TASK);
            profileData.insertResult(start, end);

            ++receivedT;
            this.violations *= 0.99;
        }
    }
    public void handleServerTransaction(short id, long now) {
        if (id < 0) {
            TaskData pendingTask = this.pings.peekLast();

            if (pendingTask != null && now - pendingTask.getTimestamp() > MathUtil.toNanos(30000L)) {
                kicklol(false, "(30s)");
            }

            TaskData taskData = new TaskData(id, now);
            postPendingTask.forEach(taskData::addTask);
            postPendingTask.clear();
            pings.push(taskData);
            ++sentTransac;
        }
    }
    public void handleServerKeepalive(long id) {
        pendingKeepalive.add(id);
        ++sentKeep;
    }
    public void queueToPrePing(Callback<Integer> callback) {
        TaskData mostRecent = pings.peek();
        if (mostRecent != null) {
            mostRecent.addTask(callback);
        } else {
            //Karhu.getInstance().printCool("&b> &fKarhu failed to find recent transaction, releasing task");
            callback.call(data.getCurrentServerTransaction());
        }
    }
    public int mostRecentPing() {
        TaskData mostRecent = pings.peek();
        if (mostRecent != null) return mostRecent.getId();
        else return data.getCurrentServerTransaction();
    }
    public void queueToPostPing(Callback<Integer> callback) {
        postPendingTask.add(new KarhuTask(callback));
    }
    private void kicklol(boolean keep, String reason) {
        if (!kicked) {
            if (Karhu.getInstance().getConfigManager().isNethandler() && Karhu.getInstance().getConfigManager().isDelay()) {
                kicked = true;
                String msg = !keep

                        ? configManager.getCancelTransactions()
                        .replaceAll("%player%", data.getName())
                        .replaceAll("%invalid%", String.valueOf(pings.size()))
                        .replaceAll("%total%", String.valueOf(sentTransac))

                        : configManager.getCancelKeepalives()
                        .replaceAll("%player%", data.getName())
                        .replaceAll("%invalid%", String.valueOf(pendingKeepalive.size()))
                        .replaceAll("%total%", String.valueOf(sentKeep));

                Tasker.run(() -> {
                    MiscellaneousAlertPoster.postMisc(msg, data, "Delay");
                    data.getBukkitPlayer().kickPlayer("Timed out " + reason);
                });
            }
        }
    }
    private void kickxd() {
        if (!kicked) {
            if (Karhu.getInstance().getConfigManager().isNethandler() && Karhu.getInstance().getConfigManager().isSpoof()) {
                kicked = true;

                MiscellaneousAlertPoster.postMisc(configManager.getOwnKeepalives()
                                .replaceAll("%player%", data.getName())
                                .replaceAll("%invalid%", String.valueOf(invalidKeepalive.size()))
                                .replaceAll("%total%", String.valueOf(sentKeep)),

                        data, "Spoof");

                Tasker.run(() -> data.getBukkitPlayer().kickPlayer(configManager.getCancelOwnKick()));
            }
        }
    }
    private void handleInvalidTransaction(short id, short first) {
        if (configManager.isNethandler() && configManager.isDelay()) {
            if (!kicked) {
                kicked = true;

                Tasker.run(() -> {
                    data.getBukkitPlayer().kickPlayer(configManager.getOrderKick()
                            .replaceAll("%first%", String.valueOf(first))
                            .replaceAll("%received%", String.valueOf(id))
                    );
                });

                MiscellaneousAlertPoster.postMisc(configManager.getTransactionOrder()
                                .replaceAll("%player%", data.getName())
                                .replaceAll("%first%", String.valueOf(first))
                                .replaceAll("%sent%", String.valueOf(id)),
                        data, "Spoof");
            }
        }
    }

    public void onTeleportConfirmPacket() {
        Teleport teleport = data.getTeleportManager().locations.peek();
        TaskData taskStorage = pings.peek();
        if (teleport != null && taskStorage != null && teleport.timestamp > taskStorage.getTimestamp()) {
            Tasker.run(() -> {
                MiscellaneousAlertPoster.postMiscPrivate("Accepting teleport but not transactions");
                data.getBukkitPlayer().kickPlayer("Invalid teleport");
            });
        }
    }
}
