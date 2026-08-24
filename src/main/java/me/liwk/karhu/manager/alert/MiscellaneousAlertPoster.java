package me.liwk.karhu.manager.alert;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.manager.ConfigManager;
import me.liwk.karhu.util.discord.Webhook;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class MiscellaneousAlertPoster {

    public static void postMisc(String debug, KarhuPlayer data, String type) {


        final String message = Karhu.getInstance().getConfigManager().getMiscPrefix() + debug;
        Karhu.getInstance().getAlertsManager().getAlertsToggled()
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(staff -> staff.sendMessage(message));

        if(type.contains("Crash")) {
            handleDiscord(debug, data);
        }
    }

    public static void postMiscTp(BaseComponent hover) {
        Karhu.getInstance().getAlertsManager().getMiscDebugToggled()
                .stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                .forEach(admin -> admin.spigot().sendMessage(hover));
    }

    public static void postMiscPrivate(String msg) {
        Karhu.getInstance().getAlertsManager().getMiscDebugToggled()
                .stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                .forEach(admin -> admin.sendMessage(msg));
    }

    public static void postMitigation(KarhuPlayer data, double vl, String check, String debug) {
        ConfigManager configManager = Karhu.getInstance().getConfigManager();
        String cmd = configManager.getAlertClickCommand();

        BaseComponent hover = createHoverComponent(data, vl, check, configManager);

        if (cmd != null) {
            hover.setClickEvent(createClickEvent(data, cmd));
        }

        hover.setHoverEvent(createHoverEvent(data, check, debug, configManager));

        sendAlertToAdmins(hover);
    }

    private static BaseComponent createHoverComponent(KarhuPlayer data, double vl, String check, ConfigManager configManager) {
        return new TextComponent(configManager.getPrefix() + configManager.getMitigationMessage()
                .replace("%player%", data.getName())
                .replace("%version%", formatVersion(data.getClientVersion()))
                .replace("%brand%", data.getBrand())
                .replace("%ping%", String.valueOf(data.getTransactionPing()))
                .replace("%tps%", String.valueOf(Karhu.getInstance().getTPS()))
                .replace("%check%", check)
                .replace("%vl%", String.format("%.2f", vl)));
    }

    private static ClickEvent createClickEvent(KarhuPlayer data, String cmd) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                cmd.replace("%player%", data.getBukkitPlayer().getName()));
    }

    private static HoverEvent createHoverEvent(KarhuPlayer data, String check, String debug, ConfigManager configManager) {
        String finalDebug = ChatColor.translateAlternateColorCodes('&',
                configManager.getAlertHoverMessage()
                        .replace("%info%", debug.replace("§b", configManager.getAlertHoverMessageHighlight()))
                        .replace("%player%", data.getName())
                        .replace("%ping%", String.valueOf(data.getTransactionPing()))
                        .replace("%world%", data.getBukkitPlayer().getWorld().getName())
                        .replace("%ticks%", String.valueOf(data.getTotalTicks()))
                        .replace("%client%", data.getCleanBrand())
                        .replace("%check%", check)
                        .replace("%version%", formatVersion(data.getClientVersion()))
                        .replace("%tps%", String.valueOf(Karhu.getInstance().getTPS())));

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(finalDebug).create());
    }

    private static void sendAlertToAdmins(BaseComponent hover) {
        Karhu.getInstance().getAlertsManager().getMitigationToggled()
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(admin -> admin.spigot().sendMessage(hover));
    }

    private static String formatVersion(Object version) {
        return version.toString().replace("_", ".").replace("v.", "");
    }

    public static void postSetback(String msg) {
        Karhu.getInstance().getAlertsManager().getSetbackToggled()
                .stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                .forEach(admin -> admin.sendMessage(msg));
    }

    public static void handleDiscord(String data, KarhuPlayer pdata) {

        Player player = pdata.getBukkitPlayer();

        String hookURL = Karhu.getInstance().getConfigManager().getConfig().getString("discord.crash-webhook-url");

        final Webhook discord = new Webhook(hookURL);

        boolean showWorld = Karhu.getInstance().getConfigManager().getConfig().getBoolean("discord.show-world");
        boolean showStats = Karhu.getInstance().getConfigManager().getConfig().getBoolean("discord.show-statistics");
        boolean showIcon = Karhu.getInstance().getConfigManager().getConfig().getBoolean("discord.show-icon-thumbnail");

        discord.setUsername(Karhu.getInstance().getConfigManager().getName());
        discord.setTts(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        if (showIcon) {
            discord.addEmbed(new Webhook.EmbedObject()
                    .setTitle("```" +player.getName() + "``` | Crash ")
                    .setThumbnail("https://minotar.net/avatar/" + player.getName() + "/50.png")
                    .setDescription(ChatColor.stripColor(data.replaceAll("\n", "")))
                    .setColor(Color.ORANGE)
                    .addField("Info",
                            (showWorld ? "World: " + player.getWorld().getName() + " | Coords: " + format(1, player.getLocation().getX()) + "/" + format(1, player.getLocation().getY()) + "/" + format(1, player.getLocation().getZ())
                                    : " Coords: " + format(1, player.getLocation().getX()) + "/" + format(1, player.getLocation().getY()) + "/" + format(1, player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + pdata.getTransactionPing() + "ms" : ""), false)
                    .addField("Date", dtf.format(now), false));
        } else {
            discord.addEmbed(new Webhook.EmbedObject()
                    .setTitle("```" +player.getName() + "``` | Crash ")
                    .setDescription(ChatColor.stripColor(data.replaceAll("\n", "")))
                    .setColor(Color.ORANGE)
                    .addField("Info",
                            (showWorld ? "World: " + player.getWorld().getName() + " | Coords: " + format(1, player.getLocation().getX()) + "/" + format(1, player.getLocation().getY()) + "/" + format(1, player.getLocation().getZ())
                                    : " Coords: " + format(1, player.getLocation().getX()) + "/" + format(1, player.getLocation().getY()) + "/" + format(1, player.getLocation().getZ())) + (showStats ? " | TPS: " + Karhu.getInstance().getTPS() + " | Ping: " + pdata.getTransactionPing() + "ms" : ""), false)
                    .addField("Date", dtf.format(now), false));
        }
        try {
            discord.execute();
        } catch (IOException ex) {
            if (ex.toString().contains("429")) {
                Karhu.getInstance().getLogger().warning("Unable to post discord webhook: 429 Too many requests");
            } else if(!ex.getMessage().contains("no protocol")){
                Karhu.getInstance().getLogger().warning("Unable to post discord webhook: " + ex.getMessage());
            }
        }
    }
    private static String format(int places, Object obj) {
        return String.format("%." + places + "f", obj);
    }
}