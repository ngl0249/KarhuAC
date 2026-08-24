package me.liwk.karhu.check.impl.combat.autoclicker;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.SwingEvent;

@CheckInfo(name = "AutoClicker (A)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = false)
public final class AutoClickerA extends PacketCheck {

    public AutoClickerA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private int delay, clicks;

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if(canClick()) {
                if(data.isNewerThan8()) {
                    if(data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 60L) {
                        ++clicks;
                    }
                } else {
                    ++clicks;
                }
            }

        } else if (packet instanceof FlyingEvent) {
            if(++delay >= 20) {
                if(clicks >= 1) {
                    data.setLastCps(data.getCps());
                    data.setCps(clicks);

                    data.setHighestCps(Math.max(clicks, data.getHighestCps()));
                }

                if(clicks > Karhu.getInstance().getConfigManager().getMaxCps() && !data.isHasDig()) {
                    fail("* Too high cps" +
                            "\n §f* CPS: §b" + clicks, 300L);
                }
                delay = 0;
                clicks = 0;
            }
        }
    }
}
