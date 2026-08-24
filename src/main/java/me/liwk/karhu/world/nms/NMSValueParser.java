package me.liwk.karhu.world.nms;

import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.pair.Pair;

import java.util.*;

public final class NMSValueParser {

    public static final List<float[]> KEY_COMBOS = Collections.synchronizedList(Arrays.asList(
            new float[]{0F, 0F},
            new float[]{0F, 1F},
            new float[]{1F, 1F},
            new float[]{-1F, 1F},
            new float[]{1F, 0F},
            new float[]{1F, -1F},
            new float[]{0F, -1F},
            new float[]{-1F, -1F},
            new float[]{-1F, 0F}
    ));

    public static final List<float[]> KEY_COMBOS_MOVING = Collections.synchronizedList(Arrays.asList(
            new float[]{0F, 1F},
            new float[]{1F, 1F},
            new float[]{-1F, 1F},
            new float[]{1F, 0F},
            new float[]{1F, -1F},
            new float[]{0F, -1F},
            new float[]{-1F, -1F},
            new float[]{-1F, 0F},
            new float[]{0F, 0F}
    ));

    public static final boolean[] BOOLEANS = new boolean[]{true, false};
    public static final boolean[] BOOLEANS_REVERSED = new boolean[]{false, true};

    //todo - rethink the static use
    public static void parse(KarhuPlayer data) {

        data.setLastJumpMovementFactor(data.getJumpMovementFactor());
        data.setJumpMovementFactor(data.getSpeedInAir());
        data.setJumpMovementFactor(data.isWasSprinting() ? (float) ((double) data.getJumpMovementFactor() + (double) 0.02F * 0.3D) : data.getJumpMovementFactor());

        double attri = data.getWalkSpeed();

        if (data.isSprinting()) {
            attri += attri * 0.3F;
        }

        data.setLastAttributeSpeed(data.getAttributeSpeed());
        data.setAttributeSpeed((float) attri);

        double jumpMotion = 0.42f;
        jumpMotion += data.getJumpBoost() * 0.1F;

        double difference = data.deltas.motionY - jumpMotion;

        data.setJumpedLastTick(data.isJumpedCurrentTick());
        data.setJumpedCurrentTick(((Math.abs(difference) <= 0.03125 && data.isLastOnGroundPacket() && !data.isOnGroundPacket())
                || (data.isLastOnGroundPacket() && !data.isOnGroundPacket() && data.elapsed(data.getLastCollidedV()) <= 1 && data.deltas.motionY > 0)
                || data.recentlyTeleported(2)));
        data.setJumped(data.isJumpedCurrentTick());
    }

    public static double moveFlying(KarhuPlayer data, float strafe, float forward, boolean sprint) {

        float friction = moveEntityWithHeading(data, sprint).getY();

        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {

            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;

            float f1 = MathHelper.sin(data.getLocation().getYaw() * (float)Math.PI / 180.0F);
            float f2 = MathHelper.cos(data.getLocation().getYaw() * (float)Math.PI / 180.0F);

            float xAdd = strafe * f2 - forward * f1;
            float zAdd = forward * f2 + strafe * f1;

            return MathUtil.hypot(xAdd, zAdd);
        }

        return 0;
    }

    public static Pair<Float, Float> moveFlyingPair(KarhuPlayer data, float strafe, float forward, boolean sprint) {

        float friction = moveEntityWithHeading(data, sprint).getY();

        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {

            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;

            float yawRadius = data.getLocation().getYaw() * (float)Math.PI / 180.0F;

            float f1 = MathHelper.sin(yawRadius);
            float f2 = MathHelper.cos(yawRadius);

            return new Pair<>(strafe * f2 - forward * f1, forward * f2 + strafe * f1);
        }
        return null;

    }

    public static Pair<Float, Float> moveFlyingPair2(KarhuPlayer data, float strafe, float forward, float friction) {

        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {

            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;

            float yawRadius = data.getLocation().getYaw() * (float) Math.PI / 180.0F;

            float f1 = MathHelper.sin(yawRadius);
            float f2 = MathHelper.cos(yawRadius);

            return new Pair<>(strafe * f2 - forward * f1, forward * f2 + strafe * f1);
        }

        return null;

    }

    private static Vec3 getInputVector(Vec3 inputs, float friction, float yaw, boolean fastMath) {
        double inputForce = inputs.lengthSqr();
        if (inputForce < 1.0E-7D) {
            return Vec3.ZERO;
        } else {
            Vec3 vec3 = (inputForce > 1.0D ? inputs.normalizeModern() : inputs).scale(friction);
            float yawSin = MathHelper.sin(fastMath, yaw * ((float)Math.PI / 180F));
            float yawCos = MathHelper.cos(fastMath, yaw * ((float)Math.PI / 180F));
            return new Vec3(vec3.xCoord * (double)yawCos - vec3.zCoord * (double)yawSin,
                    0,
                    vec3.zCoord * (double)yawCos + vec3.xCoord * (double)yawSin);
        }
    }


    public static Pair<Float, Float> moveEntityWithHeading(KarhuPlayer data) {

        if(data.isOnWater()) {

            float f1 = data.isSprinting() && data.isNewerThan12()
                    ? 0.9F
                    : 0.8F;

            float f2 = 0.02F;
            float f3;

            if(data.getDepthStriderLevel() > 0) f3 = data.getDepthStriderLevel();
            else f3 = 0.0F;

            if (f3 > 3.0F) f3 = 3.0F;
            if (!data.isLastOnGroundPacket()) f3 *= 0.5F;
            if (f3 > 0.0F) {
                f1 += (0.54600006F - f1) * f3 / 3.0F;
                f2 += (data.getWalkSpeed() * 1.0F - f2) * f3 / 3.0F;
            }

            if (data.getDolphinLevel() > 0) {
                f1 = 0.96F;
            }

            return new Pair<>(f1, f2);
        } else {

            float f4 = 0.91F;
            float f5;

            if (data.isLastOnGroundPacket()) {
                f4 = data.getCurrentFriction();
            }

            float f = 0.16277136F / (f4 * f4 * f4);

            if (data.isLastOnGroundPacket()) {
                f5 = data.getWalkSpeed();

                f5 += f5 * 0.3F;

                f5 *= f;
            } else {
                f5 = (float) ((double) 0.02f + (double) 0.02f * 0.3D);
            }
            return new Pair<>(f4, f5);
        }
    }

    public static Pair<Float, Float> moveEntityWithHeading(KarhuPlayer data, boolean sprint) {

        if(data.isLastOnWaterOffset()) {

            float f1 = data.isSprinting() && data.isNewerThan12()
                    ? 0.9F
                    : 0.8F;

            float f2 = 0.02F;
            float f3;

            if(data.getDepthStriderLevel() > 0) f3 = data.getDepthStriderLevel();
            else f3 = 0.0F;

            if (f3 > 3.0F) f3 = 3.0F;
            if (!data.isLastOnGroundPacket()) f3 *= 0.5F;
            if (f3 > 0.0F) {
                f1 += (0.54600006F - f1) * f3 / 3.0F;
                f2 += (data.getWalkSpeed() * 1.0F - f2) * f3 / 3.0F;
            }

            if (data.getDolphinLevel() > 0) {
                f1 = 0.96F;
            }

            return new Pair<>(f1, f2);
        } else {

            float f4 = 0.91F;
            float f5;

            if (data.isLastOnGroundPacket()) {
                f4 = data.getLastTickFriction();
            }

            float f = 0.16277136F / (f4 * f4 * f4);

            if (data.isLastOnGroundPacket()) {
                f5 = data.getWalkSpeed();

                if (sprint) {
                    f5 += f5 * 0.3F;
                }

                f5 *= f;
            } else {
                f5 = sprint ? (float) ((double) 0.02f + (double) 0.02f * 0.3D) : 0.02F;
            }
            return new Pair<>(f4, f5);
        }
    }

    public static double loopKeys(KarhuPlayer data) {
        double maxSpeed = Double.MIN_VALUE;

        for (int strafe = 1; strafe >= -1; --strafe) {
            for (int forward = 1; forward >= -1; --forward) {
                //for (int sprintLoop = 2; sprintLoop > 0; sprintLoop--) {
                    float currentStrafe = strafe * 0.98F;
                    float currentForward = forward * 0.98F;

                    double moveFlying = moveFlying(data, currentStrafe, currentForward, true);

                    if(moveFlying > maxSpeed) {
                        maxSpeed = moveFlying;
                    }
                //}
            }
        }
        return maxSpeed;
    }

    public static Pair<Double, Double> loopKeysGetKeys(KarhuPlayer data, double kbX, double kbZ) {

        final Map<Double, Pair<Double, Double>> dataAssessments = new HashMap<>();

        double x = kbX;
        double z = kbZ;

        for (float[] floats : NMSValueParser.KEY_COMBOS) {
            for (boolean sprint : BOOLEANS) {
                for (boolean blocking : BOOLEANS_REVERSED) {
                    for (boolean jumped : BOOLEANS_REVERSED) {

                        float strafe = floats[0];
                        float forward = floats[1];

                        //if (sprint && forward <= 0) continue; - causes falses

                        if (jumped && sprint) {
                            float f = data.getLocation().getYaw() * 0.017453292F;
                            kbX -= MathHelper.sin(f) * 0.2F;
                            kbZ += MathHelper.cos(f) * 0.2F;
                        }

                        if (data.isWasSneaking()) {
                            strafe = (float) ((double) strafe * 0.3D);
                            forward = (float) ((double) forward * 0.3D);
                        }

                        if (blocking) {
                            strafe *= 0.2F;
                            forward *= 0.2F;
                        }

                        strafe *= 0.98F;
                        forward *= 0.98F;

                        Pair<Float, Float> xzPair = moveFlyingPair(data, strafe, forward, sprint);

                        if (xzPair != null) {
                            kbX += xzPair.getX();
                            kbZ += xzPair.getY();
                        }

                        double deltaX = data.deltas.deltaX - kbX;
                        double deltaZ = data.deltas.deltaZ - kbZ;

                        double hypot = MathUtil.hypot(deltaX, deltaZ);

                        if (hypot <= 0.001) {
                            return new Pair<>(kbX, kbZ);
                        }

                        dataAssessments.put(hypot, new Pair<>(kbX, kbZ));

                        kbZ = z;
                        kbX = x;

                    }
                }
            }
        }

        return dataAssessments.get(dataAssessments.keySet().stream().mapToDouble(d -> d).min().orElse(3865386.0D));
    }

    public static Pair<Double, Double> bruteforceAttack(KarhuPlayer data, double kbX, double kbZ) {
        final Map<Double, Pair<Double, Double>> diffs = new HashMap<>();

        double original = MathUtil.hypot(data.deltas.deltaX - kbX, data.deltas.deltaZ - kbZ);
        diffs.put(original, new Pair<>(kbX, kbZ));

        double min = data.getClientVersion().getProtocolVersion() > 47 ? 0.003D : 0.005D;

        kbX = Math.abs(kbX) < min ? 0 : kbX;
        kbZ = Math.abs(kbZ) < min ? 0 : kbZ;

        double ogX = kbX;
        double ogZ = kbZ;

        for (int j = 0; ++j <= data.getAttacks(); ) {

            ogX *= 0.6D;
            ogZ *= 0.6D;

            final Pair<Double, Double> dataX = loopKeysGetKeys(data, ogX, ogZ);

            double diffMult = MathUtil.hypot(data.deltas.deltaX - dataX.getX(), data.deltas.deltaZ - dataX.getY());

            if (diffMult <= 0.001) {
                return new Pair<>(dataX.getX(), dataX.getY());
            }

            diffs.put(diffMult, new Pair<>(dataX.getX(), dataX.getY()));

            ogX = kbX;
            ogZ = kbZ;
        }

        Pair<Double, Double> pair = diffs.get(diffs.keySet().stream().mapToDouble(d -> d).min().orElse(0));

        diffs.clear();
        
        return pair;
    }

}
