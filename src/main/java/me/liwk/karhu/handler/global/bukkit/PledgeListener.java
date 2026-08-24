package me.liwk.karhu.handler.global.bukkit;

import dev.thomazz.pledge.pinger.ClientPingerListener;

public class PledgeListener implements ClientPingerListener {

    /*@Override
    public void onPingSendStart(Player player, int id) {
        long time = System.nanoTime();
        //Bukkit.broadcastMessage("Sent first ping in tick: " + id);

        //System.out.println("ID FIRST " + id);

        KarhuPlayer user = Karhu.getInstance().getDataManager().getPlayerData(player.getUniqueId());
        user.getFrameData().handleFrameSend(id, true, time);
    }

    @Override
    public void onPingSendEnd(Player player, int id) {
        long time = System.nanoTime();
        //Bukkit.broadcastMessage("Sent second ping in tick: " + id);

        //System.out.println("ID SECOND " + id);

        KarhuPlayer user = Karhu.getInstance().getDataManager().getPlayerData(player.getUniqueId());
        user.getFrameData().handleFrameSend(id, false, time);
    }

    @Override
    public void onPongReceiveStart(Player player, int id) {
        long time = System.nanoTime();
        //Bukkit.broadcastMessage("Received first pong of tick: " + id);

        KarhuPlayer user = Karhu.getInstance().getDataManager().getPlayerData(player.getUniqueId());
        user.getFrameData().handleFrameReceive(user, true, id, time);
    }

    @Override
    public void onPongReceiveEnd(Player player, int id) {
        long time = System.nanoTime();
        //Bukkit.broadcastMessage("Received second pong of tick: " + id);

        KarhuPlayer user = Karhu.getInstance().getDataManager().getPlayerData(player.getUniqueId());
        user.getFrameData().handleFrameReceive(user, false, id, time);
    }*/
}
