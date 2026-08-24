package me.liwk.karhu.handler.global.bukkit;

import org.bukkit.event.Listener;

public class PledgeHandler implements Listener {

    /*private final ConfigManager configManager = Karhu.getInstance().getConfigManager();
    @EventHandler
    public void onReceive(PacketFrameReceiveEvent event) {
        long time = System.nanoTime();

        KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());
        if(data == null) return;

        //Bukkit.broadcastMessage("Receive " + event.getFrame().getId1());

        data.getThread().getExecutorService().execute(() -> {
            Karhu.getInstance().getTransactionHandler().handleReceiveTransaction(event, time, data);
            data.getNetHandler().handleFrame(event.getType(), event.getFrame());
        });
    }

    @EventHandler
    public void onCreate(PacketFrameCreateEvent event) {
        long time = System.nanoTime();

        KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());
        if(data == null) return;

        PacketFrame frame = event.getFrame();

        //Bukkit.broadcastMessage("Create " + event.getFrame().getId1());

        data.getThread().getExecutorService().execute(() -> {
            data.setCurrentPacketFrame(frame);
            Karhu.getInstance().getTransactionHandler().handleTransaction((short) frame.getId1(), time, data);
            data.getNetHandler().frames.put(frame, new TaskData(frame.getId1(), frame.getId2()));
        });
    }

    @EventHandler
    public void onSendEvent(PacketFrameSendEvent event) {
        long time = System.nanoTime();

        KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());
        if(data == null) return;

        PacketFrame frame = event.getFrame();

        data.getThread().getExecutorService().execute(() -> {
            Karhu.getInstance().getTransactionHandler().handleTransaction((short) frame.getId2(), time, data);
        });

    }

    @EventHandler
    public void onErrorEvent(PacketFrameErrorEvent event) {
        KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());
        if(data == null) return;

        PacketFrame frame = event.getFrame();

        String frameInput = frame != null ? frame.toString() : "null";
        String frame1 = frame != null ? String.valueOf(frame.getId1()) : "null";
        String frame2 = frame != null ? String.valueOf(frame.getId2()) : "null";

        MiscellaneousAlertPoster.postMiscPrivate("PERROR | Name: " + data.getName() + " | " + frameInput);

        if (configManager.isNethandler() && configManager.isDelay()) {
            if (!data.isBanned()) {
                data.setBanned(true);

                Tasker.run(() -> {
                    data.getBukkitPlayer().kickPlayer(configManager.getOrderKick()
                            .replaceAll("%first%", String.valueOf(frame1))
                            .replaceAll("%received%", String.valueOf(frame)
                                    + " (" + event.getType().toString() + ")")
                    );
                });

                MiscellaneousAlertPoster.postMisc(configManager.getTransactionOrder()
                                .replaceAll("%player%", data.getName())
                                .replaceAll("%first%", frame1)
                                .replaceAll("%sent%", frame2)
                                + " (" + event.getType().toString() + ")",
                        data, "Spoof");
            }
        }

    }

    @EventHandler
    public void onTimeoutEvent(PacketFrameTimeoutEvent event) {
        KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());
        if(data == null) return;

        PacketFrame frame = event.getFrame();

        MiscellaneousAlertPoster.postMiscPrivate("PTIMEOUT | Name: " + data.getName() + " | " + event.getFrame().toString());

        if (Karhu.getInstance().getConfigManager().isNethandler() && Karhu.getInstance().getConfigManager().isDelay()) {
            if (!data.isBanned()) {
                data.setBanned(true);
                String msg = configManager.getCancelTransactions()
                        .replaceAll("%player%", data.getName())
                        .replaceAll("%invalid%", String.valueOf(frame.getId1()))
                        .replaceAll("%total%", String.valueOf(frame.getId2()));

                Tasker.run(() -> {
                    MiscellaneousAlertPoster.postMisc(msg, data, "Delay");
                    data.getBukkitPlayer().kickPlayer("Timed out (20)");
                });
            }
        }
    }*/
}