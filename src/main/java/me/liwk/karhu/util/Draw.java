package me.liwk.karhu.util;

import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import me.liwk.karhu.util.player.PlayerUtil;
import org.bukkit.entity.Player;

public class Draw {

    public static void drawBox(Player player, double minX, double maxX, double minY, double maxY, double minZ, double maxZ, float accuracy) {
        // Define coordinates for box edges
        double[][] horizontalEdges = {
                {minX, maxX, minY, minY, minZ, minZ}, // Bottom horizontal edges
                {minX, maxX, minY, minY, maxZ, maxZ},
                {minX, minX, minY, minY, minZ, maxZ},
                {maxX, maxX, minY, minY, minZ, maxZ},
                {minX, maxX, maxY, maxY, minZ, minZ}, // Top horizontal edges
                {minX, maxX, maxY, maxY, maxZ, maxZ},
                {minX, minX, maxY, maxY, minZ, maxZ},
                {maxX, maxX, maxY, maxY, minZ, maxZ}
        };

        double[][] verticalEdges = {
                {minX, minX, minY, maxY, minZ, minZ}, // Vertical edges
                {minX, minX, minY, maxY, maxZ, maxZ},
                {maxX, maxX, minY, maxY, minZ, minZ},
                {maxX, maxX, minY, maxY, maxZ, maxZ}
        };

        // Draw horizontal edges
        for (double[] edge : horizontalEdges) {
            drawLine(player, edge[0], edge[1], edge[2], edge[3], edge[4], edge[5], accuracy);
        }

        // Draw vertical edges
        for (double[] edge : verticalEdges) {
            drawLine(player, edge[0], edge[1], edge[2], edge[3], edge[4], edge[5], accuracy);
        }
    }

    public static void drawLine(Player player, double x1, double x2, double y1, double y2, double z1, double z2, float accuracy) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        double deltaZ = z2 - z1;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        float n = distance / accuracy;

        // Determine the particle type outside the loop
        Particle particle = accuracy == 0.2F ? new Particle(ParticleTypes.CRIT) : new Particle(ParticleTypes.WITCH);

        for (float a = 0; a <= n; a++) {
            double x = x1 + (deltaX * (a / n));
            double y = y1 + (deltaY * (a / n));
            double z = z1 + (deltaZ * (a / n));

            PlayerUtil.sendPacket(player,
                    new WrapperPlayServerParticle(particle,
                            true, new Vector3d(x, y, z),
                            new Vector3f(0, 0, 0),
                            0, 0)
            );
        }
    }
}
