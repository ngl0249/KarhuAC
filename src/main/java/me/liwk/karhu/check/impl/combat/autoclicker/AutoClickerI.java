package me.liwk.karhu.check.impl.combat.autoclicker;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckInfo(name = "AutoClicker (I)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = false)
public final class AutoClickerI extends PacketCheck {

    public AutoClickerI(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private final Deque<Integer> delays = new ArrayDeque<>();

    private int delay;

    @Override
    public void handle(final Event packet) {
        if (packet instanceof AttackEvent) {
            boolean valid = !data.isPlacing() && !data.isHasDig() && !data.isUsingItem() && data.elapsed(data.getDigTicks()) > 5;

            if (valid) {

                if (delay <= 5 && delay > 0) delays.add(delay);

                if (delays.size() == 40) {

                    double average = MathUtil.average(delays);
                    double std = MathUtil.stdDev(average, delays);

                    if (average <= 2.0 && std < 0.15 && data.getCps() > 8) {
                        if (++violations > 10) {
                            fail("* No randomization\n§f* STD §b" + std
                                    + "\n§f* AVG §b" + average
                                    + "\n§f* CPS §b" + data.getCps(),
                                    getBanVL(), 200L);
                            decrease(violations);
                        }
                    } else {
                        decrease(violations);
                    }

                    delays.removeFirst();
                }
                delay = 0;
            }
        } else if (packet instanceof FlyingEvent) {
            delay++;
        }
    }
}

