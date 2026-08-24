package me.liwk.karhu.check.impl.combat.killaura;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;

@CheckInfo(name = "Killaura (A)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false)
public final class KillauraA extends PacketCheck {

    public Long lastUseEntity, lastFlying;

    public KillauraA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof FlyingEvent) {
            lastFlying = ((FlyingEvent) packet).getCurrentTimeMillis();
            if (lastUseEntity != null) {
                double delay = lastFlying - lastUseEntity;
                if (delay < 60L && delay > 40L && !data.hasFast() && !data.isPossiblyTeleporting() && !data.isLagging(data.getTotalTicks())) {
                    if (++violations > 3) {
                        fail("* Post killaura\n §f* D §b" + delay, getBanVL(), 600L);
                        violations = 3;
                    }
                } else {
                    violations = Math.max(violations - 0.35, 0);
                }
                lastUseEntity = null;
            }
        } else if (packet instanceof AttackEvent) {

            if (lastFlying != null && ((AttackEvent) packet).getTimeMillis() - lastFlying < 2L) {
                lastUseEntity = lastFlying;
            } else {
                violations = Math.max(violations - 0.35, 0);
            }
        }
    }
}
