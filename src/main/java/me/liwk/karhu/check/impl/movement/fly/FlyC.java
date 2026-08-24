package me.liwk.karhu.check.impl.movement.fly;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Fly (C)", category = Category.MOVEMENT, subCategory = SubCategory.FLY, experimental = false)
public final class FlyC extends PositionCheck {

    private double lastAccel, lastMotionY;

    public FlyC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        final double accel = Math.abs(this.lastMotionY - data.deltas.motionY);

        final boolean valid = !data.recentlyTeleported(1)
                && this.data.getAirTicks() > 10
                && !this.data.isTakingVertical()
                && !this.data.isOnClimbable()
                && !this.data.isOnLiquid()
                && !this.data.isInUnloadedChunk()
                && !this.data.isGliding()
                && !this.data.isCollidedHorizontally()
                && !this.data.isRiptiding()
                && !this.data.isSpectating()
                && !this.data.isOnWeb()
                && !this.data.isInWeb()
                && !this.data.isOnSlime()
                && this.data.elapsed(data.getLastInBerry()) > 3
                && !this.data.isOnGhostBlock()
                && (data.isHasReceivedTransaction() || data.getTotalTicks() > 100) //TODO BURGER
                && !(Math.abs(this.data.deltas.motionY + 3.9) <= 0.021)
                && !this.data.isAllowFlying()
                && this.data.getLevitationLevel() == 0
                && this.data.elapsed(data.getLastFlyTick()) > 80
                && !this.data.isUnderBlock();

        if (Math.abs(this.data.deltas.motionY + 0.098) <= 1E-5) {
            this.lastAccel = accel;
            return;
        }

        if (data.getLocation().y <= -5 && data.deltas.motionY == 0) {
            this.lastAccel = accel;
            return;
        }

        double acceleration = Math.abs(accel - this.lastAccel);

        double maxVL = acceleration == 0.0 ? 6.0D : 3.0D;

        if ((acceleration <= 1E-5 || accel <= 1E-10) && valid) {
            if (this.violations++ > maxVL) {
                fail("* Achieving impossible air acceleration \n §f* A: §b" + acceleration + "\n §f* ST/CT: §b" + this.data.getAirTicks() + " | " + this.data.getClientAirTicks(), getBanVL(), 300);
            }
        } else if (acceleration > 1E-5 || accel > 1E-10) {
            this.violations *= 0.75;
        }


        this.lastAccel = accel;
        this.lastMotionY = data.deltas.motionY;
    }

}
