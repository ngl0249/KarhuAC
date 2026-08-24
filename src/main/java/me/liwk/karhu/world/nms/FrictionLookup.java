package me.liwk.karhu.world.nms;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.util.KarhuStream;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class FrictionLookup {

    public static final Map<Set<Material>, Float> FRICTION_CACHE = new HashMap<>();

    static {
        FRICTION_CACHE.put(MaterialChecks.PACKEDICE, 0.98F);
        FRICTION_CACHE.put(MaterialChecks.FROSTEDICE, 0.98F);
        FRICTION_CACHE.put(MaterialChecks.CLEARICE, 0.98F);
        FRICTION_CACHE.put(MaterialChecks.BLUEICE, 0.989F);
        FRICTION_CACHE.put(MaterialChecks.SLIME, 0.8F);
    }

    public static float lookup(KarhuPlayer data) {

        final Material moveMat = data.getMovementBlock();

        if (data.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)) {

            if (moveMat != null) {

                float viaVerFriction = getViaVersionFrictions(data, moveMat);

                if (viaVerFriction == 0) {

                    Set<Material> list = new KarhuStream<>(FRICTION_CACHE.keySet()).find(s -> s.contains(moveMat));

                    return (list == null ? 0.6F : FRICTION_CACHE.get(list));// * 0.91F;
                } else {
                    return viaVerFriction;
                }
            } else {
                return 0.6F;// * 0.91F;
            }
        } else {

            Material material2 = data.getAirMovementBlock();

            if (material2 == null) {
                return 0.6F;
            }

            float viaVerFriction = getViaVersionFrictions(data, material2);

            if (viaVerFriction == 0) {

                Set<Material> list = new KarhuStream<>(FRICTION_CACHE.keySet()).find(s -> s.contains(material2));

                return (list == null ? 0.6F : FRICTION_CACHE.get(list));
            } else {
                return viaVerFriction;
            }
        }
    }

    public static float getViaVersionFrictions(KarhuPlayer data, Material material) {
        if (data.getClientVersion().getProtocolVersion() < 47
                && MaterialChecks.SLIME.contains(material))
            return 0.6F;

        if (data.getClientVersion().isOlderThan(ClientVersion.V_1_15)
                && MaterialChecks.HONEY.contains(material))
            return 0.8F;

        if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_13) && !data.isNewerThan12()) {
            if (MaterialChecks.BLUEICE.contains(material)) {
                return 0.98F;
            }
        }

        return 0;
    }
}
