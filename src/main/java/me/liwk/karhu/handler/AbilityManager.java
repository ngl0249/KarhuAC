package me.liwk.karhu.handler;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;

/*
From grim
 */
public class AbilityManager {

    private final KarhuPlayer data;

    private int badTicks = -1;
    private boolean flySet;

    public AbilityManager(KarhuPlayer data) {
        this.data = data;
    }

    public void onFlying() {
        flySet = false;

        if (badTicks == 0) {
            badTicks = 1;
        } else if (badTicks == 1) {
            data.flying = false;
            badTicks = -1;
        }

        if (Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8)) {
            data.setLastGlide(data.isGliding() ? data.getTotalTicks() : data.getLastGlide());
        }
    }

    public void onAbilityClient(WrapperPlayClientPlayerAbilities abilities) {
        boolean fly = abilities.isFlying();

        if (flySet && !abilities.isFlying()) {
            flySet = false;
            badTicks = 0;
            return;
        }

        if (abilities.isFlying()) {
            flySet = true;
        }

        data.flying = fly && data.allowFlying;
    }

    public void onAbilityServer(WrapperPlayServerPlayerAbilities packet) {
        if (!packet.isFlightAllowed()) {
            CustomLocation location = data.getLocation();

            long time = data.getServerTick() - data.getLastTeleportPacket();

            if (data.getTeleportManager().locations.isEmpty() && time > 3) {
                data.setFlyCancel(new CustomLocation(location.x, location.y, location.z));
            }

            data.confirmingFlying = true;
            data.setLastConfirmingState(data.getTotalTicks());
        }

        data.queueToPrePing((uid) -> {
            data.flying = packet.isFlying();
            data.allowFlying = packet.isFlightAllowed();

            data.confirmingFlying = false;
            badTicks = -1;
        });

    }

}
