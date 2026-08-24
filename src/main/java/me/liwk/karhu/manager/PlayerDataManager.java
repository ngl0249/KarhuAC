package me.liwk.karhu.manager;

import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {

    @Getter
    private final Map<UUID, KarhuPlayer> playerDataMap = new ConcurrentHashMap<>();

    private final Karhu karhu;

    public PlayerDataManager(Karhu karhu) {
        this.karhu = karhu;
    }


    public KarhuPlayer getPlayerData(final Player player) {
        return playerDataMap.get(player.getUniqueId());
    }
    public KarhuPlayer getPlayerData(final User user) {
        return playerDataMap.get(user.getUUID());
    }

    public KarhuPlayer getPlayerData(final UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public KarhuPlayer remove(final UUID uuid) {

        KarhuPlayer data = getPlayerData(uuid);

        if (data != null) {
            data.setRemovingObject(true);

            //Karhu.getInstance().getThreadManager().shutdownThread(data);
        }

        return playerDataMap.remove(uuid);
    }

    public KarhuPlayer add(final User user, long now) {
        if (!ChannelHelper.isOpen(user.getChannel())) return null;
        return this.playerDataMap.put(user.getUUID(), new KarhuPlayer(user, karhu, now));
    }

}
