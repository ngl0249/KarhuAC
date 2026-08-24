package me.liwk.karhu.check.impl.movement.motion;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;
import org.bukkit.util.Vector;

@CheckInfo(name = "Motion (J)", category = Category.MOVEMENT, subCategory = SubCategory.MOTION, experimental = true)
public final class MotionJ extends PositionCheck {

    private int desyncPosTicks, desyncsInRow;

    public MotionJ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate e) {

        boolean canFlag = true;

        CustomLocation to = e.getTo();
        CustomLocation from = e.getFrom();

        Vector tickVel = data.getTickedVelocity();

        double min = this.data.getClientVersion().getProtocolVersion() > 47 ? 0.003D : 0.005D;
        double threshold = min + 0.031;

        final double motionY = data.deltas.motionY;
        final double lastMotionY = data.deltas.lastMotionY;

        if (Math.abs(motionY + 0.098) <= 1E-5) {
            return;
        }

        double prediction = Math.abs((lastMotionY - 0.08) * 0.9800000190734863D) <= threshold
                ? 0
                : (lastMotionY - 0.08) * 0.9800000190734863D;

        if(tickVel != null) {
            prediction = tickVel.getY();
        }

        if(data.getTeleportManager().teleportTicks == 1) {
            prediction = 0.0D;
        }

        if((Math.abs(prediction * prediction) <= 0.03125 && data.deltas.deltaXZ < 0.15D) || desyncPosTicks > 0 || data.getMoveTicks() <= 1) {
            threshold = 0.08;
            if(desyncPosTicks <= 0) {
                desyncPosTicks = 2;
            }
            ++desyncsInRow;
        } else {
            desyncsInRow = 0;
        }

        if (data.elapsed(data.getPredictionTicks()) <= 1) {
            threshold += (0.08 * 0.98D) + data.clamp();
        }

        if(data.getTeleportManager().teleportTicks == 2) {
            threshold += 0.08;
        }

        --desyncPosTicks;

        if (data.isWasOnWater()) {
            double fixedLastMotion = lastMotionY;
            if (motionY > 0) {
                fixedLastMotion += 0.04F;
            }
            prediction = data.getVelocityYTicks() == 0 ? data.getVelocityY() : (fixedLastMotion * 0.800000011920929D) - 0.02D;
            threshold += data.isCollidedHorizontally() ? 0.4D : 0.35D;
        } else if (data.isOnWater()) {
            threshold += 0.4D;
        }

        if (data.isWasOnLava()) {
            double fixedLastMotion = lastMotionY;
            if(motionY > 0) {
                fixedLastMotion += 0.04F;
            }
            prediction = data.getVelocityYTicks() == 0 ? data.getVelocityY() : (fixedLastMotion * 0.5D) - 0.02D;
            threshold += data.isCollidedHorizontally() ? 0.4D : 0.35D;
        } else if (data.isOnLava()) {
            threshold += 0.4D;
        }

        int lastInLiquid = data.elapsed(data.getLastInLiquid());
        if (!data.isWasOnWater() && lastInLiquid > 2 && lastInLiquid <= 5 + data.getPingInTicks()) {
            threshold += 0.1;
        }


        if (data.elapsed(data.getLastRelativeVelo()) <= 8) {
            threshold += 0.5;
        }

        double predictionDifference = Math.abs(prediction - data.deltas.motionY);

        //Fix for clamp
        if (Math.abs(lastMotionY - 0.083) < 1E-3D && predictionDifference > 0.078) {
            prediction = 0;
            predictionDifference = Math.abs(prediction - data.deltas.motionY);
        }

        double offsetPlace = Math.abs(lastMotionY - 0.404444914);
        double offsetPlacePrediction = Math.abs(predictionDifference - 0.01524);

        //Exempt for groundstates
        if ((to.ground && motionY < 0.0D && prediction < motionY && MathUtil.onGround(Math.abs(to.getY()))))
            canFlag = false;

        //Exempt for jumpboost near 0 motion
        if((from.horizontal(to) < 0.0025D && data.getJumpBoost() > 0))
            canFlag = false;

        //Fix for 0.03 on places
        if (offsetPlace <= 1E-5D && offsetPlacePrediction <= 1E-3D) {
            threshold += 0.015625D;
        } else {
            if(data.elapsed(data.getUnderPlaceTicks()) <= 10) {
                threshold += 0.031;
            }
        }

        /*if(data.elapsed(data.getPredictionTicks()) == 0) {
            threshold += 0.031;
        }*/

        final boolean underBlock = data.elapsed(data.getLastCollidedV()) <= 2 || data.isUnderBlock()
                || data.elapsed(data.getLastCollidedVGhost()) <= 3 || data.isUnderGhostBlock();
        final boolean climbable = data.elapsed(data.getLastOnClimbable()) <= 8 || data.isOnLadder();
        final boolean slime = data.elapsed(data.getLastOnSlime()) <= 2 || data.isOnSlime();
        final boolean web = data.elapsed(data.getLastInWeb()) <= 5 || data.isInWeb();
        final boolean piston = data.elapsed(data.getLastPistonPush()) <= 3;
        //final boolean liquid = data.elapsed(data.getLastInLiquid()) <= 3 || data.isOnLiquid();

        double predictionOffset = Math.abs(motionY - prediction);

        double maxVL = tickVel != null ? 4.5 : 2.5;

        if(data.isUnderGhostBlock()) {
            violations /= 2;
        }

        if(data.elapsed(data.getLastFlyTick()) < 80) {
            threshold += 0.8;
        }

        if (Math.abs(predictionOffset) > threshold + min
                && Math.abs(prediction) >= threshold + min
                && canFlag
                && this.data.elapsed(this.data.getLastGlide()) >= 30
                && this.data.elapsed(this.data.getLastRiptide()) >= 30
                && !data.isUnderGhostBlock()
                && !data.isOnLiquid()
                && !data.rodPullAffecting()
                && this.data.elapsed(data.getLastInPowder()) > 3
                && this.data.elapsed(data.getLastInBerry()) > 3
                && !data.isSpectating()
                && !this.data.isInsideBlock()
                && this.data.getSlowFallingLevel() == 0
                && this.data.getLevitationLevel() == 0
                && !data.isPossiblyTeleporting()
                && this.data.elapsed(data.getLastFlyTick()) > 30) {
            if(!underBlock && !climbable && !slime && !web && !piston && !data.isUnderWeb()) {
                if(!e.to.ground && !e.from.ground && data.getAirTicks() > 0) {
                    if (++violations > maxVL) {
                        final String info = String.format("predict: %.3f, motionY: %.3f" +
                                        "\nthreshold: %f, ct/st: %d/%d" +
                                        "\nteleport: %d" +
                                        "\nvelocity: %.4f" +
                                        "\nmove: %d" +
                                        "\ndeltaX/deltaZ: %.3f/%.3f",
                                prediction, motionY,
                                threshold, data.getClientAirTicks(), data.getAirTicks(),
                                data.getTeleportManager().teleportTicks,
                                tickVel != null ? tickVel.getY() : 0,
                                data.getMoveTicks(),
                                data.deltas.deltaX, data.deltas.deltaZ);

                        fail(info, getBanVL(), 200L);
                    }
                }
            } else violations = Math.max(violations - 0.06235, 0.0);
        } else violations = Math.max(violations - 0.065, 0.0);
    }
}

