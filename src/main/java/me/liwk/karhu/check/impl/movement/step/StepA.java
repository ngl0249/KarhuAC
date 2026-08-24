package me.liwk.karhu.check.impl.movement.step;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Step (A)", category = Category.MOVEMENT, subCategory = SubCategory.MOTION, experimental = false)
public final class StepA extends PositionCheck {

    public StepA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        float max = 0.6F;

        double offsetY = data.deltas.motionY, lastOffsetY = data.deltas.lastMotionY;

        if (this.data.getVelocityY() > 0) {
            max = (float) (Math.max(max, this.data.getVelocityY() + 0.6f));
        }

        max += PlayerUtil.getJumpBooster(data);

        boolean newSwimming = data.isNewerThan12();

        if (data.elapsed(this.data.getLastInLiquid()) <= (newSwimming ? 10 : 3)) {
            max += newSwimming ? 1.2D : 0.6D;
        }

        if (this.data.elapsed(this.data.getLastOnSlime()) <= 50) {
            max += 4F;
        }

        if (lastOffsetY < -0.4 && data.elapsed(data.getPlaceTicks()) < 15) {
            max += 4F;
        }

        boolean valid = !data.isOnPiston()
                && !data.isWasOnSlime()
                && data.getTeleportManager().teleportTicks > 1
                && !data.isSpectating()
                && data.elapsed(data.getLastPistonPush()) > 2
                && data.levitationLevel == 0
                && !data.isOnBed()
                && data.elapsed(data.getLastRodPullTick()) > ((data.getRodPullLeniencyY() + data.getLastRodPullLeniencyY()) * 3) + 10 //YIKES
                && !data.isWasOnBed()
                && data.elapsed(data.getLastRiptide()) > 30
                && data.elapsed(data.getLastGlide()) > 30
                && this.data.elapsed(data.getLastFlyTick()) > 40;

        if (this.data.elapsed(this.data.getLastGlide()) <= 100 || this.data.elapsed(this.data.getLastRiptide()) <= 100) {
            max += 6F;
        }

        if (data.elapsed(data.getLastJumpBoostChange()) <= 3) {
            max += 2F;
        }

        if (offsetY > max && valid) {
            if ((violations += (offsetY - max) + 0.5) > 1) {
                fail("* Jumping higher than expected" +
                        "\n §f* D: §b" + this.format(3, offsetY) +
                        "\n §f* M: §b" + max +
                        "\n §f* JC: §b" + data.elapsed(data.getLastJumpBoostChange()) +
                        "\n §f* TP: §b" + this.data.getTeleportManager().teleportTicks, getBanVL(), 300L);
            }
        } else {
            this.violations *= 0.1;
        }

        if (!update.from.ground && update.fromFrom.ground && valid && checkBlocks()) {
            double difference = offsetY - lastOffsetY;
            double combined = offsetY + lastOffsetY;
            if (difference > 0 && offsetY > 0 && lastOffsetY > 0 && combined > max && !data.isTakingVertical()) {
                fail("* Jumping higher than expected (V2)" +
                        "\n §f* D: §b" + this.format(3, offsetY) +
                        "\n §f* M: §b" + max +
                        "\n §f* DIFF: §b" + this.format(3, difference) +
                        "\n §f* TP: §b" + this.data.getTeleportManager().teleportTicks, getBanVL(), 300L);
            }
        }

        if (checkBlocks() && valid) {
            if (offsetY > 0.001 && lastOffsetY > 0 && update.isGround() && !data.isTakingVertical()) {
                fail("* Impossible upwards movement (GROUND)" +
                                "\n §f* D/LD: §b" + this.format(3, offsetY) + "/" + this.format(3, lastOffsetY),
                        getBanVL(), 300L);
            }
        }
    }

    public boolean checkBlocks() {
        return data.elapsed(data.getLastOnSlime()) > 1
                && data.elapsed(data.getLastOnHalfBlock()) > 2
                && data.elapsed(data.getLastFence()) > 2
                && data.elapsed(data.getLastPortal()) > 2
                && data.elapsed(data.getLastInPowder()) > 1
                && data.elapsed(data.getLastOnBoat()) > 2;
    }
}
