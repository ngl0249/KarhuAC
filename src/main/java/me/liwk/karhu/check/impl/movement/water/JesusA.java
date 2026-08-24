package me.liwk.karhu.check.impl.movement.water;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Jesus (A)", category = Category.MOVEMENT, subCategory = SubCategory.JESUS, experimental = true)
public final class JesusA extends PositionCheck {

    public JesusA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {


        if ((data.isOnLiquid() || data.isAboveButNotInWater())
                && !data.isPossiblyTeleporting()
                && data.elapsed(data.getUnderPlaceTicks()) > data.getPingInTicks() + 5
                && !data.isOnGhostBlock()
                && data.elapsed(data.getLastPistonPush()) > 3
                && data.elapsed(data.getLastFlyTick()) > 30) {
            if (data.getAirTicks() > 4 && update.isGround()) {
                if (++violations > 3) {
                    fail("* Wrong groundstate on liquid" +
                            "\n§f* Inside §b" + data.isOnLiquid() +
                            "\n§f* Above §b" + data.isAboveButNotInWater(), getBanVL(), 300);
                }
            } else {
                violations = Math.max(violations - 0.25, 0);
            }
        }
    }
}
