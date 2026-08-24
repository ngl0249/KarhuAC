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
import me.liwk.karhu.util.MathUtil;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.liwk.karhu.util.MathUtil.average;

@CheckInfo(name = "AutoClicker (C)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = false)
public final class AutoClickerC extends PacketCheck {

    public AutoClickerC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private final Deque<Integer> delays = new ArrayDeque<>();
    private int delay;

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (checkClick()) {
                if (data.isNewerThan8()) {
                    if (delay < 10 && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                        delays.add(delay);
                } else {
                    if (delay < 10)
                        delays.add(delay);
                }

                if (delays.size() == 800) {

                    int outliers = MathUtil.getOutliers(delays);
                    double cps = 20.0 / average(delays);

                    if (outliers <= 5) {
                        if (++violations > 1) {
                            fail("* Low outliers\n §f* O: §b" + outliers + "\n §f* CPS: §b" + cps, getBanVL(), 450L);
                        }

                    } else {
                        violations = Math.max(violations - 0.5, 0);
                    }

                    delays.clear();

                }
            }
            delay = 0;
        } else if (packet instanceof FlyingEvent) {
            if(data.isUsingItem()) {
                delay = 0;
                return;
            }
            ++delay;
        }
    }
}
