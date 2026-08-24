package me.liwk.karhu.util.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.liwk.karhu.Karhu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class BungeeAPI {

    public static void sendCommand(String args) {
        sendPluginMessage("karhu:bban", args);
    }

    public static void sendAlert(String msg) {
        sendPluginMessage("karhu:alert", msg);
    }

    public static void sendPluginMessage(final String subChannel, final String... args) {
        final Player messenger = getRandomPlayer();

        if (messenger != null) {
            final ByteArrayDataOutput out = ByteStreams.newDataOutput();

            out.writeUTF(subChannel);

            for (final String arg : args) {
                out.writeUTF(arg);
            }

            messenger.sendPluginMessage(Karhu.getInstance(), Karhu.getInstance().getBungeeChannel(), out.toByteArray());
        } else {
            Karhu.getInstance().printCool("No player found for " + subChannel + "!");
        }
    }

    public static Player getRandomPlayer() {
        final Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

        if (!players.isEmpty()) {
            final int i = (int) ((players.size()) * Math.random());

            return players.toArray(new Player[0])[i];
        }

        return null;
    }
}
