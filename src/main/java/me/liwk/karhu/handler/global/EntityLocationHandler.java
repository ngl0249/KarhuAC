package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;

public class EntityLocationHandler {

    public static void addEntity(KarhuPlayer data, EntityType entityType, double x, double y, double z, int eid) {
        EntityData ed = new EntityData(x, y, z, eid, entityType, 0);

        data.entityData.put(eid, ed);
    }

    public static void addEntity(KarhuPlayer data, EntityType entityType, double x, double y, double z, int eid, int owner) {
        EntityData ed = new EntityData(x, y, z, eid, entityType, owner);

        data.entityData.put(eid, ed);
    }

    public static void updateEntityLook(KarhuPlayer data, int id) {
        EntityData edata = data.entityData.get(id);
        if (edata == null) return;

        //edata.newLocations.add(new Vector3d(edata.newX, edata.newY, edata.newZ));

        edata.posIncrements = edata.isBoat() ? 10 : 3;

        /*if ((data.getClientVersion().isNewerThan(ClientVersion.V_1_21_4) ||
                        (data.getClientVersion().isOlderThan(ClientVersion.V_1_20_2))
                                && data.getClientVersion().isNewerThan(ClientVersion.V_1_14_4))) {
            edata.cancelInterpolation();
        }*/
    }

    //Double transaction
    public static void updateEntityRelMove2(KarhuPlayer data, int id, double x, double y, double z) {
        EntityData edata = data.entityData.get(id);
        if (edata == null) return;

        edata.newX += x;
        edata.newY += y;
        edata.newZ += z;

        edata.newLocations.add(new Vector3d(edata.newX, edata.newY, edata.newZ));

        if (data.getClientVersion().getProtocolVersion() > 47) {
            lenientBox(edata.newX, edata.newY, edata.newZ, edata);
        }

        edata.posIncrements = edata.isBoat() ? 10 : 3;

        edata.cancelledLerpSteps = 0;
        edata.originalPosIncrements = 0;

    }

    public static void updateEntityTeleport2(KarhuPlayer data, int id, double x, double y, double z, boolean interp) {
        EntityData edata = data.entityData.get(id);
        if (edata == null) return;

        edata.newX = x;
        edata.newY = y;
        edata.newZ = z;

        edata.newLocations.add(new Vector3d(edata.newX, edata.newY, edata.newZ));

        if (data.getClientVersion().getProtocolVersion() > 47) {
            lenientBox(x, y, z, edata);
        }

        edata.posIncrements = edata.isBoat() ? 10 : 3;

        edata.cancelledLerpSteps = 0;
        edata.originalPosIncrements = 0;
    }

    /*
    Interpolation
     */
    public static void updateEntityLocations(KarhuPlayer data) {
        data.entityData.values().forEach(EntityData::interpolate);
    }

    public static void updateFlyingLocations(KarhuPlayer data, WrapperPlayClientPlayerFlying flying) {
        //Bukkit.broadcastMessage("updated flying loc");
        if (flying.hasPositionChanged()) {
            data.lastPos = 0;
            data.attackerX = flying.getLocation().getX();
            data.attackerY = flying.getLocation().getY();
            data.attackerZ = flying.getLocation().getZ();
        } else data.lastPos++;

        if (flying.hasRotationChanged()) {
            data.attackerYaw = flying.getLocation().getYaw();
            data.attackerPitch = flying.getLocation().getPitch();
        }
    }

    public static void destroyEntity(KarhuPlayer data, int[] id) {
        for (int a : id) {
            data.entityData.remove(a);

            for (EntityData edata : data.entityData.values()) {
                if (edata.isRiding() && a == edata.getVehicleId()) {
                    edata.setRiding(false);
                    edata.setVehicleId(-1);
                }
            }

            if (data.isRiding()) {
                if (a == data.getVehicleId()) {
                    data.setRiding(false);
                    data.setBrokenVehicle(true);
                    data.setVehicleId(-1);
                    data.setVehicle(null);
                }
            }
        }
    }

    private static void lenientBox(double x, double y, double z, EntityData edata) {
        edata.maxX = Math.max(edata.maxX, x);
        edata.minX = Math.min(edata.minX, x);
        edata.maxY = Math.max(edata.maxY, y);
        edata.minY = Math.min(edata.minY, y);
        edata.maxZ = Math.max(edata.maxZ, z);
        edata.minZ = Math.min(edata.minZ, z);
    }
}
