package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import lombok.RequiredArgsConstructor;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.Teleport;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.task.Tasker;

import java.util.LinkedList;

@RequiredArgsConstructor
public final class TeleportManager {

    private final KarhuPlayer data;

    public final LinkedList<Teleport> locations = new LinkedList<>();

    public int teleportAmount, zeroAmount, teleportTicks, teleportsPending, trackedTps, tickSinceConfirm;

    public void handlePreFlying(WrapperPlayClientPlayerFlying packet) {

        final boolean legacy = data.getClientVersion().getProtocolVersion() < 47,
                ground = !legacy && packet.isOnGround(),
                moving = packet.hasPositionChanged(),
                rotating = packet.hasRotationChanged();

        Location location = packet.getLocation();

        if (!locations.isEmpty()) {

            for (Teleport teleport : locations) {

                Vector3d position = checkRelatives(location, teleport);

                double distance = teleport.position.distance(position);

                //data.getBukkitPlayer().sendMessage("§6Scanned teleport " + data.getTotalTicks() + " | " + distance);

                double diff = hasRelativeFlags(teleport) ? 5 : 1E-7;

                if (distance <= diff && moving && rotating && !ground) {
                    teleportTicks = 0;
                    ++zeroAmount;
                    ++trackedTps;

                    data.setForceRunCollisions(true);

                    //removeLocation(teleport);
                    //data.getBukkitPlayer().sendMessage("§aFound teleport " + data.getTotalTicks() + " | " + distance);

                    callTeleport(position);
                    break;
                }
            }
        }

        data.setPossiblyTeleporting(isTeleporting());
    }

    public void findTeleport(WrapperPlayClientPlayerFlying packet) {
        this.tickSinceConfirm++;
        boolean moving = packet.hasPositionChanged();
        boolean rotating = packet.hasRotationChanged();
        Teleport teleport = locations.peek();
        if (teleport != null && teleport.accepted) {
            boolean validFormat = !packet.isOnGround() && moving && rotating;
            boolean validLocation = atExpectedLocation(packet.getLocation(), teleport);
            if (!validFormat || !validLocation || this.tickSinceConfirm > 1) {

                Tasker.run(() -> {
                    MiscellaneousAlertPoster.postMiscPrivate("Teleport format");
                    data.getBukkitPlayer().kickPlayer(String.format("Invalid teleport format %b %b %d", validFormat, validLocation, this.tickSinceConfirm));
                });
            }

            teleportTicks = 0;
            ++zeroAmount;
            ++trackedTps;

            data.setForceRunCollisions(true);
            callTeleport(null);
            locations.poll();
        }
    }

    public void setTeleporting() {
        this.tickSinceConfirm++;

        teleportTicks = 0;
        ++zeroAmount;
        ++trackedTps;

        data.setForceRunCollisions(true);
        callTeleport(null);
    }

    public boolean atExpectedLocation(Location location, Teleport teleport) {
        CustomLocation from = null;
        double maxDifference = 1E-10;
        if (hasRelativeFlags(teleport)) {

            maxDifference += 5;

            from = data.getLastLocation();
        }
        Vector3d expectedPosition = checkRelatives(from, teleport);

        CustomLocation location1 = new CustomLocation(location.getX(), location.getY(), location.getZ());

        return location1.distance(expectedPosition.x, expectedPosition.y, expectedPosition.z) <= maxDifference;
    }

    public void handlePostFlying() {
        ++teleportTicks;
    }

    public boolean isTeleporting() {
        return teleportTicks <= 0;
    }

    public void removeLocation(Teleport teleport) {
        locations.remove(teleport);
        --teleportsPending;
    }
    
    private void callTeleport(Vector3d vector) {
        data.setHasTeleportedOnce(true);
        data.getLocation().setTeleport(true);
    }

    public void confirmingTeleport(WrapperPlayClientTeleportConfirm packet) {
        if (data.legacyTeleports()) {
            return;
        }
        Teleport teleport = locations.peek();
        if (teleport == null) {
            Tasker.run(() -> {
                MiscellaneousAlertPoster.postMiscPrivate("Teleport unknown");
                data.getBukkitPlayer().kickPlayer("Unknown teleport confirm " + packet.getTeleportId());
            });
            return;
        }
        if (teleport.teleportId != packet.getTeleportId()) {
            Tasker.run(() -> {
                MiscellaneousAlertPoster.postMiscPrivate("Teleport unknown");
                data.getBukkitPlayer().kickPlayer("Wrong teleport id " + packet.getTeleportId());
            });
            return;
        }
        tickSinceConfirm = 0;
        teleport.setAccepted(true);
    }

    private Vector3d checkRelatives(Location loc, Teleport teleport) {
        double x = teleport.position.getX();
        if (teleport.relativeFlag.has(RelativeFlag.X.getMask())) {
            x += loc.getX();
        }
        double y = teleport.position.getY();
        if (teleport.relativeFlag.has(RelativeFlag.Y.getMask())) {
            y += loc.getY();
        }
        double z = teleport.position.getZ();
        if (teleport.relativeFlag.has(RelativeFlag.Z.getMask())) {
            z += loc.getZ();
        }
        return new Vector3d(x, y, z);
    }

    private Vector3d checkRelatives(CustomLocation loc, Teleport teleport) {
        double x = teleport.position.getX();
        if (teleport.relativeFlag.has(RelativeFlag.X.getMask())) {
            x += loc.getX();
        }
        double y = teleport.position.getY();
        if (teleport.relativeFlag.has(RelativeFlag.Y.getMask())) {
            y += loc.getY();
        }
        double z = teleport.position.getZ();
        if (teleport.relativeFlag.has(RelativeFlag.Z.getMask())) {
            z += loc.getZ();
        }
        return new Vector3d(x, y, z);
    }

    private boolean hasRelativeFlags(Teleport teleport) {
        if (teleport.relativeFlag.has(RelativeFlag.X.getMask())) {
            return true;
        }
        if (teleport.relativeFlag.has(RelativeFlag.Y.getMask())) {
            return true;
        }
        return teleport.relativeFlag.has(RelativeFlag.Z.getMask());
    }
}
