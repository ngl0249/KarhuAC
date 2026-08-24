package me.liwk.karhu.check.impl.combat.reach;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.impl.combat.hitbox.HitboxA;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.TickEndEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MovingObjectPosition;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.mc.vec.Vec3;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Reach (A)", category = Category.COMBAT, subCategory = SubCategory.REACH, experimental = false)
public final class ReachA extends PacketCheck {

    private boolean flyingBeforeTick;

    public ReachA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        if (packet instanceof FlyingEvent) {
            handleReach(((FlyingEvent) packet).hasLooked());

            flyingBeforeTick = true;

        } else if (packet instanceof TickEndEvent) {
            /*if (!flyingBeforeTick && data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
                handleReach(false);
            }

            flyingBeforeTick = false;
            */

        } else if (packet instanceof AttackEvent) {
            ClientVersion version = data.getClientVersion();

            if (!data.isPossiblyTeleporting()) {
                for (int target : data.getLastTargets()) {
                    EntityData edata = data.getEntityData().get(target);

                    if (edata != null) {
                        if (data.isNewerThan8()) {
                            double dist = data.getBoundingBox().distanceToHitbox(edata.getEntityBoundingBox());

                            if (Math.abs(data.getLocation().pitch) != 90F)
                                dist /= Math.cos(Math.toRadians(data.getLocation().pitch));

                            if (dist > data.getInteractionRange() + 1) {
                                Karhu.getInstance().printCool("&b> &fKarhu USER: " + data.getBukkitPlayer().getName() + " is reaching far " + dist);
                                data.setEntityIdCancel(target);
                            }
                            data.setReduceNextDamage(dist > data.getInteractionRange() + 1);
                        }
                    } else {
                        data.setReduceNextDamage(true);
                        data.setEntityIdCancel(target);
                    }
                }
            }

            if (!data.isForceCancelReach()) {
                int max = version.getProtocolVersion() <= 47 ? 3 : 21;
                data.setCancelTripleHit(data.getAttacks() > max);
            }

        }
    }

    private void handleReach(boolean look) {
        for (int target : data.getLastTargets()) {
            if (data.getGameMode() != GameMode.CREATIVE
                    //&& target instanceof Player
                    && !data.isRiding()
                    && data.elapsed(data.getYawFucked()) > 1
                    //&& target.getVehicle() == null
                    && !data.isSpectating()
                    && !data.isPossiblyTeleporting()) {

                if (data.getLastAttackTick() > 1) return;

                EntityData edata = data.getEntityData().get(target);


                if (edata == null) {
                    data.setReachBypass(true);
                    return;
                }

                if (edata.isRiding()) {
                    data.setReachBypass(true);
                    return;
                }

                float pitch = data.getLocation().pitch, yaw = data.getLocation().yaw;

                List<Vec3> rotationVectors = new ArrayList<>();

                data.setReachBypass(false);
                AxisAlignedBB box = edata.getEntityBoundingBox();
                AxisAlignedBB axisalignedbb = !edata.uncertainBox
                        ? MathUtil.getHitbox(data, box)
                        : MathUtil.getHitbox(data, box)
                        .union(MathUtil.getHitbox(data, edata.getEntityBoundingBoxLast())).expand(0.1, 0.1, 0.1);

                axisalignedbb.expand(0.003, 0.003, 0.003); //Fuck it

                double distance = Double.MAX_VALUE;

                for (double height : data.getEyePositions()) {

                    if (look) {
                        rotationVectors.add(MathUtil.getVectorForRotation(pitch, yaw, data));

                        if (data.getClientVersion().getProtocolVersion() >= 47) {
                            rotationVectors.add(MathUtil.getVectorForRotation(pitch, data.attackerYaw, data));
                        }

                    } else {
                        rotationVectors.add(MathUtil.getVectorForRotation(data.attackerPitch, data.attackerYaw, data));
                    }

                    for (Vec3 rLook : rotationVectors) {

                        Vec3 eyeLocation = new Vec3(data.attackerX, data.attackerY + height, data.attackerZ);

                        double range = data.getInteractionRange() + 3;

                        Vec3 search = eyeLocation.addVector(
                                rLook.xCoord * range,
                                rLook.yCoord * range,
                                rLook.zCoord * range
                        );

                        MovingObjectPosition intercept = axisalignedbb.calculateIntercept(eyeLocation, search);

                        if (axisalignedbb.isVecInside(eyeLocation)) {
                            distance = 0;
                            break;
                        }

                        if (intercept != null) {

                            double dist = eyeLocation.distanceTo(intercept.hitVec);

                            if (dist < distance) {
                                distance = dist;
                            }

                            if (Karhu.getInstance().getAlertsManager().hasDebugToggled(data.getBukkitPlayer())
                                    && distance > data.getInteractionRange() && distance != Double.MAX_VALUE)
                                data.getBukkitPlayer().sendMessage(format(4, distance));

                        }
                    }
                }

                double x = box.getCenterX();
                double z = box.getCenterZ();

                double direction = MathUtil.getDirection(data.getLocation(), new Vector(x, 0.0D, z));
                double angle = MathUtil.getAngleDistance(data.getLocation().getYaw(), direction);
                double dist = data.getLastBoundingBox().distanceToHitbox(edata.getEntityBoundingBox());

                if (Math.abs(data.getLocation().pitch) != 90F)
                    dist /= Math.cos(Math.toRadians(data.getLocation().pitch));

                final double buffer = Math.max(cfg.getReachBuffer(), 1.2);
                final double removal = Math.max(cfg.getReachDecayPerMiss(), 0.0015);

                double minReach = Math.max(cfg.getReachToFlag(), data.getInteractionRange());

                boolean checkReach = true;

                if (distance == Double.MAX_VALUE) {
                    if (Karhu.getInstance().getConfigManager().isCheckHitbox()) {
                        if (++subVl > (angle > 20 ? 2 : 4) + (dist > 8 ? 1 : 2)) {
                            data.getCheckManager().getCheck(HitboxA.class)
                                    .fail("* Hit out of the box" +
                                            "\n * dist §b" + dist +
                                            "\n * angle §b" + angle +
                                            "\n * mins §b" + edata.minX + " / " + edata.minY + " / " + edata.minZ +
                                            "\n * maxs §b" + edata.maxX + " / " + edata.maxY + " / " + edata.maxZ +
                                            "\n * locations §b" + edata.newLocations.size() +
                                            "\n * existed §b" + edata.getExist() +
                                            "\n §f* DEV DATA: §b" + edata.posIncrements,
                                     300L);
                            data.setCancelNextHitH(true);
                        } else {
                            data.getCheckManager().getCheck(HitboxA.class)
                                    .debug(String.format("A: %.3f D: %.3f B: %.3f", angle, dist, subVl));
                        }
                    }

                    checkReach = false;

                } else {
                    subVl = Math.max(subVl - 0.175, 0);
                    data.setCancelNextHitH(false);
                }

                if (distance > data.getHighestReach()) {
                    data.setHighestReach(distance);
                }

                if (distance >= minReach && checkReach) {
                    violations += Math.min(1, (distance - minReach) + 0.3);
                    if (violations >= buffer) {
                        fail("§f* Longer arms"
                                        + "\n §f* Range: §b" + distance
                                        + "\n §f* DEV DATA: §b" + edata.posIncrements
                                        + "\n §f* existed: §b" + edata.getExist()
                                        + "\n §f* locations: §b" + edata.newLocations.size() + " | " + edata.isUncertainBox()
                                        + "\n §f* DEV DATA: §b" + data.getTeleportManager().zeroAmount + "/" + data.getTeleportManager().teleportAmount,
                                getBanVL(), 300L);
                        data.setCancelNextHitR(true);
                    }
                } else if (checkReach) {
                    decrease(removal);
                    data.setCancelNextHitR(false);
                }
            }
        }
    }
}