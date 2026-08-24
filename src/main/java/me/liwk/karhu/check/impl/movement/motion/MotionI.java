package me.liwk.karhu.check.impl.movement.motion;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Motion (I)", category = Category.MOVEMENT, subCategory = SubCategory.MOTION, experimental = true)
public final class MotionI extends PositionCheck {

    public MotionI(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate update) {

        double motionY = data.deltas.motionY, lastMotionY = data.deltas.lastMotionY;

        double minimum = data.getMoveTicks() <= 1 ? -0.2D : -0.1D;

        if (data.isLegacy()) minimum = -0.8D;

        if (data.getTotalTicks() > 40
                && !data.isPossiblyTeleporting()
                && !data.isRiding()
                && !data.isInBed()
                && !data.isLastInBed()
                && data.elapsed(data.getLastRelativeVelo()) > 2
                && data.elapsed(data.getLastFlyTick()) > 30
                && data.elapsed(data.getLastInPowder()) > 10
                && data.elapsed(data.getLastRiptide()) > 5
                && data.elapsed(data.getLastGlide()) > 5
                && this.data.elapsed(data.getLastPistonPush()) > 30) {

            if (motionY < -3.92005) {
                final String data = "* Too high vertical motion downwards" +
                        "\n§f* motY: §b" + format(3, motionY) +
                        "\n§f* vehicle: §b" + this.data.isRiding() +
                        "\n§f* teleport §b" + this.data.getTeleportManager().teleportTicks;
                this.fail(data, getBanVL(), 200);
            } else if (lastMotionY == 0.0D && motionY < minimum
                    && !data.isOnSlime() && !data.isTakingVertical()
                    && data.getTeleportManager().teleportTicks > 1
                    && !data.isOnLiquid()) {
                final String data = "* Invalid vertical motion downwards" +
                        "\n§f* motY: §b" + format(3, motionY) + " | " + format(3, lastMotionY) +
                        "\n§f* vehicle: §b" + this.data.isRiding() +
                        "\n§f* teleport §b" + this.data.getTeleportManager().teleportTicks;
                this.fail(data, getBanVL(), 200);
            }

        }

    }
}
