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

@CheckInfo(name = "AutoClicker (L)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = false)
public final class AutoClickerL extends PacketCheck {

    public AutoClickerL(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private final Deque<Integer> delays = new ArrayDeque<>();

    private int delay;

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            boolean valid = !data.isPlacing() && !data.isHasDig() && !data.isUsingItem() && data.elapsed(data.getDigTicks()) > 5;

            if(valid) {

                if (data.isNewerThan8()) {
                    if (delay < 10 && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                        delays.add(delay);
                } else {
                    if (delay < 10)
                        delays.add(delay);
                }

                if (delays.size() == 150) {
                    double std = MathUtil.getStandardDeviation(delays);
                    double cps = 20.0 / average(delays);
                    if (std < 0.445) {
                        if (++violations > 3) {
                            fail("* Poor randomization\n§f* STD §b" + MathUtil.getStandardDeviation(delays) + "\n§f* CPS §b" + cps, getBanVL(), 200L);
                        }
                    } else {
                        decrease(0.65D);
                    }
                    delays.clear();
                }
                delay = 0;
            }
        } else if (packet instanceof FlyingEvent) {
            delay++;
        }
    }
}

