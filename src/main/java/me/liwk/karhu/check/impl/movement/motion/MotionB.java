package me.liwk.karhu.check.impl.movement.motion;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Motion (B)", category = Category.MOVEMENT, subCategory = SubCategory.MOTION, experimental = false)
public final class MotionB extends PositionCheck {

    private double slimeHeight;

    public MotionB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        double deltaY = data.deltas.motionY;

        int jumpLevel = data.getJumpBoost();

        if (jumpLevel < 10 &&
                !this.data.isPossiblyTeleporting() &&
                !this.data.isOnBed() &&
                !this.data.isWasOnBed() &&
                this.data.elapsed(data.getLastOnBed()) > 5 &&
                !this.data.isSpectating() &&
                !this.data.isTakingVertical() &&
                !data.rodPullAffecting() &&
                this.data.getGameMode() != GameMode.CREATIVE &&
                this.data.elapsed(data.getLastFlyTick()) > 30 &&
                this.data.elapsed(data.getLastInBerry()) > 3 &&
                this.data.elapsed(data.getLastInPowder()) > 6 &&
                !this.data.isInUnloadedChunk() &&
                this.data.levitationLevel == 0 &&
                this.data.deltas.motionY > 0 &&
                this.data.elapsed(data.getLastInLiquid()) > 1) {

            /*
            This is required to not cause bypasses on just walking on slabs, but I will figure it later
            double jumpMax = PlayerUtil.getJumpHeight(data, (data.elapsed(data.getLastOnHalfBlock()) <= 1
                    && data.elapsed(data.getLastCollidedH()) <= 1) ? 0.565F : 0.42F);
             */

            double jumpMax = PlayerUtil.getJumpHeight(data, (data.elapsed(data.getLastOnHalfBlock()) <= 1 ? 0.565F : 0.42F));
            double maximum = PlayerUtil.getJumpHeight(data, 0.5F);
            double stepHeight = PlayerUtil.getJumpHeight(data, 0.6F);

            if (data.elapsed(data.getLastOnBoat()) <= 3) {
                maximum = PlayerUtil.getJumpHeight(data, 0.601F);
            }

            if (data.getMoveTicks() <= 1 && data.elapsed(data.getLastOnHalfBlock()) > 1) {
                jumpMax += 0.03125;
                stepHeight += 0.03125;
                jumpMax += 0.03125;
            }

            if (this.data.elapsed(this.data.getLastOnSlime()) <= 50 || data.elapsed(data.getLastSlimePistonPush()) < 30) {
                maximum += 3.5D;
                stepHeight += 3.5D;
                jumpMax += 3.5D;
            }

            if (data.isNewerThan8() && (this.data.elapsed(this.data.getLastGlide()) <= 100 || this.data.elapsed(this.data.getLastRiptide()) <= 100)) {
                jumpMax += 8D;
                maximum += 8D;
                stepHeight += 8D;
            }

            if (data.elapsed(data.getLastJumpBoostChange()) <= 3) {
                jumpMax += 2F;
                maximum += 2F;
                stepHeight += 2F;

            }

            if (this.data.isLastOnGroundPacket() && !this.data.isOnGroundPacket() && data.getTickedVelocity() == null) {
                if (this.data.deltas.motionY > (jumpMax + 0.001)) {

                    double addition = Math.abs((deltaY - jumpMax) + 0.3);

                    if (deltaY > maximum) {
                        fail("* Jumping higher than expected" +
                                " \n §f* mY: §b" + this.format(3, deltaY)
                                + " \n §f* mJ: §b" + maximum, getBanVL(), 300);
                    } else if((this.violations += addition) > 2) {
                        fail("* Jumping higher than expected" +
                                " \n §f* mY: §b" + this.format(3, deltaY)
                                + " \n §f* mJM: §b" + jumpMax, getBanVL(), 300);
                    }
                } else {
                    this.violations = Math.max(this.violations - 0.25, 0);
                }
            } else if (update.isGround()) {
                if (deltaY > (stepHeight + 0.001) && data.isLastOnGroundPacket()) {
                    fail("* Stepped higher than expected on ground" +
                            " \n §f* mY: §b" + this.format(3, deltaY)
                            + " \n §f* sH: §b" + stepHeight, getBanVL(), 300);
                    //Bukkit.broadcastMessage("§cSori " + data.getTotalTicks());
                } else if (deltaY > (jumpMax + 0.001)
                        && this.data.deltas.lastMotionY > (jumpMax + 0.001)
                && data.elapsed(data.getLastOnHalfBlock()) > 3) {
                    if (++violations > 1) {
                        fail("* Stepped higher than expected on ground" +
                                " \n §f* mY: §b" + this.format(3, deltaY)
                                + " \n §f* jumpMaxFG: §b" + jumpMax, getBanVL(), 300);
                    }
                } else {
                    violations = Math.max(violations - 0.3, 0);
                }
            }
        }
    }
}
