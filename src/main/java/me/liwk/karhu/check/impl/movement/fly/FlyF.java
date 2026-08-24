package me.liwk.karhu.check.impl.movement.fly;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Fly (F)", category = Category.MOVEMENT, subCategory = SubCategory.FLY, experimental = false)
public final class FlyF extends PositionCheck {

    private Double lastMotionY;

    public FlyF(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate e) {

        double min = data.clamp();

        double motionY = data.deltas.motionY;

        if (lastMotionY != null) {

            if (!data.isPossiblyTeleporting()) {
                if (data.elapsed(data.getLastFlyTick()) > 10) {
                    if (!data.isTakingVertical()) {

                        double prediction = Math.abs((data.deltas.lastMotionY - 0.08) * 0.98F) < min
                                ? -0.08 * 0.98F
                                : (data.deltas.lastMotionY - 0.08) * 0.98F;

                        double chunkMove = data.getLocation().y > 0.0D ? 0.09800000190735147D : 0;

                        double chunk = Math.abs(motionY + chunkMove);

                        if (chunk <= 1E-7D) {
                            lastMotionY = null;
                            return;
                        }

                        if (data.isOnGhostBlock()) {
                            lastMotionY = null;
                            return;
                        }

                        if (e.to.ground && motionY < 0.0 && prediction < motionY && MathUtil.onGround(Math.abs(e.to.getY()))
                                || e.from.horizontal(e.to) < min && data.getJumpBoost() > 0) {
                            this.setMotion(e.to, e.from);
                            return;
                        }

                        double lenience = 0.0325D;

                        if (data.getJumpBoost() > 0) {
                            lenience += 0.02; //Extra leniency for jumpboost
                        }

                        int lastInLiquid = data.elapsed(data.getLastInLiquid());

                        if (lastInLiquid <= 10) { //Lunar client, nice job
                            lenience += 0.08;
                        }

                        int tpTicks = data.getTeleportManager().teleportTicks;

                        if (Math.abs(this.data.deltas.motionY + 0.078D) < 1E-2 && Math.abs(this.data.deltas.motionY - prediction) > 0.078) {
                            prediction = !data.isNewerThan8() ? -0.08D * 0.98F : 0;
                        }

                        double difference = Math.abs(prediction - motionY);

                        if (difference > lenience && motionY < 0 && Math.abs(prediction) > min + 1E-3 && tpTicks > 1) {
                            if (!incompatibility()) {

                                if (++violations > 3.5) {

                                    fail("* Downwards gravity modification\n" +
                                            format(3, prediction - motionY) +
                                            " (" + format(4, data.deltas.motionY) + "/" + format(10, prediction) + ")\n" +
                                            tpTicks,
                                            getBanVL(), 300L);

                                }


                            } else {
                                decrease(0.08);
                            }
                        } else {
                            decrease(0.05);
                        }
                    }
                }
            }

        }


        this.setMotion(e.to, e.from);
    }

    public void setMotion(CustomLocation to, CustomLocation from) {
        if (!to.ground || !from.ground) {
            lastMotionY = to.getY() - from.getY();
        } else {
            lastMotionY = null;
        }
    }

    public boolean incompatibility() {
        double motionY = data.deltas.motionY;
        //return data.isWasOnGroundServer();
        return data.elapsed(data.getLastCollidedV()) <= 2
                || data.elapsed(data.getLastOnSlime()) <= 40
                || data.elapsed(data.getLastGlide()) <= 30
                || data.elapsed(data.getLastRiptide()) <= 30
                || data.isOnCarpet()
                || data.elapsed(data.getLastPistonPush()) < 3
                || data.isOnFence()
                || data.isWasOnFence()
                || data.isWasOnGroundServer()
                || data.elapsed(data.getLastOnHoneySide()) <= 3
                || data.isOnGhostBlock()
                || data.isNearClimbable()
                || this.data.elapsed(data.getLastInPowder()) <= 6
                || this.data.elapsed(data.getLastInBerry()) <= 3
                || data.getLevitationLevel() != 0
                || data.getSlowFallingLevel() != 0
                || data.elapsed(data.getLastOnClimbable()) <= 1
                || data.elapsed(data.getLastInLiquid()) <= 3
                || (data.elapsed(data.getLastOnHalfBlock()) <= 6 && (motionY >= 0.5F || (motionY < 0.0 && motionY > -0.2)))
                || data.isInWeb() || data.isWasInWeb();
    }
}
