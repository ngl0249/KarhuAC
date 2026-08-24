package me.liwk.karhu.handler.crash;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import lombok.RequiredArgsConstructor;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.interfaces.ICrashHandler;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.location.CustomLocation;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public final class CrashHandler implements ICrashHandler {

    private final KarhuPlayer data;

    private double nonMoves;
    private double fPackets;
    private int customPayloads;
    private int slotPackets;
    private int armAnimations;
    private int windowClicks, windowClicks2;
    private int places;
    private int lastSlot;

    private boolean logged;

    @Override
    public void handleFlying(boolean moved, boolean looked, CustomLocation location, CustomLocation lastLocation) {
        if(++fPackets > (data.getClientVersion().isOlderThan(ClientVersion.V_1_9) ? 300 : 900)) {
            if(shouldPunish("Move spam")) {
                handleKickAlert("Move spam");
            }
        }

        if(moved) {
            if (!data.isPossiblyTeleporting() &&
                    !data.recentlyTeleported(3) &&
                    (this.data.deltas.deltaXZ > 1E2
                            || (Math.abs(this.data.deltas.motionY)
                            + (data.getJumpBoost() * 0.1F) > 1E5))) {
                if(shouldPunish("Large move")) {
                    handleKickAlert("Large move");
                }
            }
        }

        armAnimations = 0;
        slotPackets = 0;
        windowClicks = 0;
        windowClicks2 = 0;
        customPayloads = 0;
        places = 0;
    }

    @Override
    public void handleArm() {
        if(++armAnimations > 200) {
            if(shouldPunish("Arm")) {
                handleKickAlert("Arm");
            }
        }
    }

    @Override
    public void handleSlot() {
        if(++slotPackets > 200) {
            if(shouldPunish("Slot")) {
                handleKickAlert("Slot");
            }
        }
    }

    @Override
    public void handleWindowClick(int slot, int mode, int id, int button) {
        if((Math.abs(slot - lastSlot) == 1 && id == 0 && ++this.windowClicks > 50) || ++this.windowClicks2 > (data.isNewerThan8() ? 125 : 5)) {
            if(shouldPunish("Window")) {
                handleKickAlert("Window");
            }
        }
    }

    @Override
    public void handleClientKeepAlive() {
        fPackets = 0;
    }

    @Override
    public void handleCustomPayload() {
        if(++customPayloads > 30) {
            if(shouldPunish("Payload")) {
                handleKickAlert("Payload");
            }
        }
    }

    @Override
    public void handlePlace() {
        if(++places > 200) {
            if(shouldPunish("Place")) {
                handleKickAlert("Place");
            }
        }
    }

    public boolean shouldPunish(String type) {

        if(Karhu.getInstance().getConfigManager().isAnticrash()) {

            switch (type) {
                case "Place":
                    if(!Karhu.getInstance().getConfigManager().isPlaceSpam()) return true;
                    break;
                case "Arm":
                    if(!Karhu.getInstance().getConfigManager().isArmSpam()) return true;
                    break;
                case "Slot":
                    if(!Karhu.getInstance().getConfigManager().isSlotSpam()) return true;
                    break;
                case "Window":
                    if(!Karhu.getInstance().getConfigManager().isWindowSpam()) return true;
                    break;
                case "Large move":
                    if(!Karhu.getInstance().getConfigManager().isLargeMove()) return true;
                    break;
                case "Move spam":
                    if(!Karhu.getInstance().getConfigManager().isMoveSpam()) return true;
                    break;
                case "Payload":
                    if(!Karhu.getInstance().getConfigManager().isPayloadSpam()) return true;
                    break;
                default:
                    return false;
            }
        }
        return false;

    }

    public void handleKickAlert(String type) {
        if (!logged) {
            data.getUser().sendPacket(new WrapperPlayServerDisconnect(fixMessage(Karhu.getInstance().getConfigManager().getAnticrashKickMsg())));
            data.getUser().closeConnection();
            //Tasker.run(() -> data.getBukkitPlayer().kickPlayer(Karhu.getInstance().getConfigManager().getAnticrashKickMsg()));
            MiscellaneousAlertPoster.postMisc(Karhu.getInstance().getConfigManager().getAntiCrashMessage().replaceAll("%debug%", type).replaceAll("%player%", data.getName()), data, "Crash");
            Karhu.getInstance().getLogger().warning("-----------------Karhu Anticrash-----------------");
            Karhu.getInstance().getLogger().warning(this.data.getName() + " was kicked for suspicious packets (" + type + ")");
            Karhu.getInstance().getLogger().warning("Keep an eye on the player!");
            Karhu.getInstance().getLogger().warning("-----------------Karhu Anticrash-----------------");
            logged = true;
        }
    }

    public Component fixMessage(String msg) {
        return Karhu.getInstance().getComponentSerializer().deserialize(msg);
    }
}
