package me.liwk.karhu.check.setback;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class KnockbackUtil {

    public static void applyKnockbackWithAbsoluteTeleports(KarhuPlayer data, Vector3d direction, int steps) {
        // Normalize the direction and scale by the total knockback strength

        // Divide the knockback into smaller steps
        Vector3d stepKnockback = direction.multiply(1.0 / steps);

        // Use a wrapper object to store the mutable position
        class PositionWrapper {
            Vector3d position;

            PositionWrapper(Vector3d position) {
                this.position = position;
            }
        }

        PositionWrapper currentPosition = new PositionWrapper(data.getLocation().toVector3d());

        // Create a task to send multiple teleport packets
        new BukkitRunnable() {
            int currentStep = 0;

            @Override
            public void run() {
                if (currentStep >= steps) {
                    data.setMitigatingVelocity(false);
                    data.setAbusingVelocity(false);
                    this.cancel(); // Stop the task once all steps are completed
                    return;
                }

                // Update the position by adding the step knockback
                currentPosition.position = currentPosition.position.add(stepKnockback);

                Block block = new Location(data.getBukkitPlayer().getWorld(),
                        currentPosition.position.x,
                        currentPosition.position.y,
                        currentPosition.position.z
                ).getBlock();

                if (block.getType().isSolid()) {
                    data.setMitigatingVelocity(false);
                    data.setAbusingVelocity(false);
                    this.cancel(); // Stop the task to prevent glitching
                    return;
                }

                // Send a teleport packet to move the player to the new position
                sendAbsoluteTeleportPacket(data, currentPosition.position);

                // Increment step counter
                currentStep++;
            }
        }.runTaskTimer(Karhu.getInstance(), 0L, 1L); // Run every tick (20 times per second)
    }
    private static void sendAbsoluteTeleportPacket(KarhuPlayer data, Vector3d position) {
        // Get the player's yaw and pitch
        float yaw = data.getLocation().getYaw();
        float pitch = data.getLocation().getPitch();

        // Convert the position to Vector3d for PacketEvents
        Vector3d absolutePosition = new Vector3d(
                position.getX(),
                position.getY(),
                position.getZ()
        );

        // Create the teleport packet with absolute positioning
        WrapperPlayServerPlayerPositionAndLook packet = new WrapperPlayServerPlayerPositionAndLook(
                0,
                absolutePosition,
                new Vector3d(0, 0, 0),
                yaw,
                pitch,
                RelativeFlag.NONE
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(data.getBukkitPlayer(), packet);
    }

    public static Vector3d getDirection(float yaw) {
        // Convert yaw from degrees to radians for trigonometric functions
        double yawRadians = Math.toRadians(yaw);

        // Calculate the direction vector pointing directly backwards
        // This means adding 180 degrees to the current yaw
        double backwardYawRadians = yawRadians + Math.PI;

        float x = (float) -Math.sin(backwardYawRadians);
        float z = (float) Math.cos(backwardYawRadians);

        // Return the normalized direction vector (y component is 0)
        return new Vector3d(x, 0, z).normalize();
    }
}
