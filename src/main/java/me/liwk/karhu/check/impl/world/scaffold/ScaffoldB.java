package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;

@CheckInfo(name = "Scaffold (B)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = false)
public final class ScaffoldB extends PacketCheck {

    public Long lastSwing, lastFlying;

    public ScaffoldB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof FlyingEvent) {
            lastFlying = ((FlyingEvent) packet).getCurrentTimeMillis();
            if (lastSwing != null) {
                double delay = lastFlying - lastSwing;
                if (delay < 60L && delay > 40L
                        && !Karhu.getInstance().isViaRewind()
                        && !data.hasFast()
                        && !data.isPossiblyTeleporting()
                        && !data.isLagging(data.getTotalTicks())) {
                    if (++violations > 3) {
                        fail("* Post swing\n §f* D §b" + delay, getBanVL(), 60000L);
                    }
                } else {
                    violations = Math.max(violations - 0.35, 0);
                }
                lastSwing = null;
            }
        } else if (packet instanceof SwingEvent) {

            if (lastFlying != null && ((SwingEvent) packet).getTimeStampMS() - lastFlying < 2L) {
                lastSwing = lastFlying;
            } else {
                violations = Math.max(violations - 0.35, 0);
            }
        }
    }
}
