package me.liwk.karhu.check.impl.movement.motion;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Motion (A)", category = Category.MOVEMENT, subCategory = SubCategory.MOTION, experimental = false)
public final class MotionA extends PositionCheck {

    public MotionA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        float jumpHeight = PlayerUtil.getJumpHeight(data);

        if (data.getJumpBoost() < 10 &&
                !data.isOnSlime() &&
                data.isInitialized() &&
                data.deltas.motionY > 0 &&
                data.elapsed(data.getLastRiptide()) > 15 &&
                data.elapsed(data.getLastGlide()) > 100 &&
                !data.isRiptiding() &&
                !data.isOnClimbable() &&
                !data.isGliding() &&
                !data.isOnBed() &&
                !data.isWasOnBed() &&
                !data.isInsideBlock() &&
                !data.isOnPiston() &&
                data.elapsed(data.getBedTicks()) > 2 &&
                this.data.elapsed(data.getLastInBerry()) > 3 &&
                data.elapsed(data.getLastPistonPush()) > 2 &&
                data.elapsed(data.getPredictionTicks()) > 3 &&
                data.elapsed(data.getLastFlyTick()) > 30 &&
                data.getClientAirTicks() == 1 &&
                data.elapsed(data.getLastInLiquid()) > 4 &&
                !data.isInWeb() &&
                !data.isWasInWeb() &&
                !data.isUnderGhostBlock() &&
                !data.isPossiblyTeleporting()) {

            float min = getMin(jumpHeight);

            final double maxVL = data.elapsed(data.getLastVelocityTaken()) <= 12 ? 7.0 : 4;
            final int tpTicks = data.getTeleportManager().teleportTicks;

            if (data.deltas.motionY < min - 0.005 && tpTicks > 1) {
                if (++this.violations > maxVL) {
                    fail("* Jumping lower than expected" +
                            "\n §f* M: §b" + min +
                            "\n §f* D: §b" + this.format(3, data.deltas.motionY) + " tk " + data.isTakingVertical() +
                            "\n §f* T: §b" + tpTicks, getBanVL(), 150);
                }
            } else {
                this.violations = Math.max(violations - 0.2, 0);
            }

        }

    }

    private float getMin(float jumpHeight) {
        float min = (!data.isOnHoney()
                && !data.isWasOnHoney()
                && !MaterialChecks.HONEY.contains(data.getMovementBlock()))
                ? jumpHeight : jumpHeight * 0.5F;

        min = data.getTickedVelocity() != null ? (float) data.getVelocityY() - 0.25F : min;

        min = (data.isUnderBlock() || data.isWasUnderBlock()) ? 0 : min;
        min -= (data.isOnSoulsand() || data.isWasOnSoulSand()) ? 0.08F : 0;
        min -= data.elapsed(data.getPlaceTicks()) > 8 && data.elapsed(data.getPlaceTicks()) <= 30 ? 0.03F: 0;
        min -= this.data.elapsed(this.data.getLastOnSlime()) <= 15 ? 0.375F : 0;
        min -= this.data.isWasOnDoor() ? 0.03F : 0;
        min -= data.getBukkitPlayer().getMaximumNoDamageTicks() <= 10 ? 0.1F : 0;

        if (data.getMoveTicks() <= 3) {
            min -= 0.03F;
        }
        return min;
    }

}
