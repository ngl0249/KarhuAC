package me.liwk.karhu.check.impl.combat.hitbox;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;

@CheckInfo(name = "Hitbox (A)", category = Category.COMBAT, subCategory = SubCategory.REACH, experimental = false)
public final class HitboxA extends PacketCheck {

    public HitboxA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        /*if(Karhu.getInstance().getConfigManager().isDisableHitboxCheck()) return;
        if(Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_19)) return;

        if (packet instanceof FlyingEvent) {

            for(int target : data.getLastTargets()) {

                if (data.getGameMode() != GameMode.CREATIVE
                        && data.getClientVersion() != null
                        && !data.isRiding()
                        && data.getBukkitPlayer().getVehicle() == null
                        //&& target.getVehicle() == null
                        && !data.isPossiblyTeleporting()
                        && data.getLastAttackTick() <= 1) {

                    EntityData edata = data.getEntityData().get(target);

                    if (edata == null) {
                        data.setReachBypass(true);
                        return;
                    }

                    if(edata.isRiding()) {
                        data.setReachBypass(true);
                        return;
                    }
                    
                    float sneakAmount1_8 = data.isWasSneaking() || data.isWasWasSneaking() ? 1.54F : data.isGliding() ? 0.4F : data.isRiptiding() ? 0.4F : 1.62F;
                    float sneakAmount1_13 = data.isWasSneaking() || data.isWasWasSneaking() ? 1.27F : data.isGliding() ? 0.4F : data.isRiptiding() ? 0.4F : 1.62F;

                    Vec3 eyeLocation = MathUtil.getPositionEyes(data.attackerX, data.attackerY, data.attackerZ,
                            !data.isNewerThan12()
                                    ? sneakAmount1_8 : sneakAmount1_13);

                    Vec3 lookMouseDelayFix;
                    Vec3 look;

                    if (((FlyingEvent) packet).hasLooked()) {
                        lookMouseDelayFix = MathUtil.getVectorForRotation(((FlyingEvent) packet).getPitch(), ((FlyingEvent) packet).getYaw(), data);
                        look = MathUtil.getVectorForRotation(((FlyingEvent) packet).getPitch(), data.attackerYaw, data);
                    } else {
                        lookMouseDelayFix = MathUtil.getVectorForRotation(data.attackerPitch, data.attackerYaw, data);
                        look = lookMouseDelayFix;
                    }

                    Vec3 vec31 = look;
                    Vec3 vec311 = lookMouseDelayFix;

                    Vec3 vec32 = eyeLocation.addVector(vec31.xCoord * 7.5D, vec31.yCoord * 7.5D, vec31.zCoord * 7.5D);
                    Vec3 vec322 = eyeLocation.addVector(vec311.xCoord * 7.5D, vec311.yCoord * 7.5D, vec311.zCoord * 7.5D);

                    boolean missed = false;

                    data.setReachBypass(false);
                    AxisAlignedBB box = edata.getEntityBoundingBox();

                    AxisAlignedBB axisalignedbb = !edata.uncertainBox
                            ? MathUtil.getHitboxLenient(data, box)
                            : MathUtil.getHitboxLenient(data, box).
                            union(MathUtil.getHitboxLenient(data, edata.getEntityBoundingBoxLast()));

                    double x = box.getCenterX();
                    double z = box.getCenterZ();

                    double direction = MathUtil.getDirection(data.getLocation(), new Vector(x, 0.0D, z));
                    double angle = MathUtil.getAngleDistance(data.getLocation().getYaw(), direction);

                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(eyeLocation, vec32);
                    MovingObjectPosition movingobjectposition2 = axisalignedbb.calculateIntercept(eyeLocation, vec322);

                    if (movingobjectposition == null && movingobjectposition2 == null && !axisalignedbb.isVecInside(eyeLocation)) {
                        missed = true;
                    }

                    double dist = data.getLastBoundingBox().distance(edata.getEntityBoundingBox());

                    double addition = dist > 2.5 ? 1D : 0.75D;

                    if (missed && data.getLastTarget().getVehicle() == null) {

                        if ((violations += addition) > (angle > 20 ? 2 : 4) + (dist > 8 ? 1 : 2) + (angle < 6 ? 2 : 0)) {
                            fail("* Hit out of the box" +
                                            "\n * dist §b" + dist +
                                            "\n * angle §b" + angle +
                                            "\n * mins §b" + edata.minX + " / " + edata.minY + " / " + edata.minZ +
                                            "\n * maxs §b" + edata.maxX + " / " + edata.maxY + " / " + edata.maxZ +
                                            "\n * locations §b" + edata.newLocations.size() +
                                            "\n * existed §b" + edata.getExist() +
                                            "\n §f* DEV DATA: §b" + edata.posIncrements,
                                    getBanVL(), 300L);
                        }

                        debug(String.format("A: %.3f D: %.3f B: %d", angle, dist, violations));

                        data.setCancelNextHitH(true);

                    } else {
                        violations = Math.max(violations - 0.215, 0);
                        data.setCancelNextHitH(false);
                    }
                }
            }
        }*/
    }
}
