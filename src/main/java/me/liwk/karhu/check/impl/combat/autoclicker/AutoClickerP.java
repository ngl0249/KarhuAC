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

@CheckInfo(name = "AutoClicker (P)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerP extends PacketCheck {

    public AutoClickerP(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private final Deque<Integer> delays = new ArrayDeque<>(), delays2 = new ArrayDeque<>();

    private int delay;

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            boolean valid = !data.isPlacing() && !data.isHasDig() && !data.isUsingItem() && data.elapsed(data.getDigTicks()) > 4;

            if (data.isNewerThan8()) {
                if (delay < 10 && valid && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                    delays.add(delay);
            } else {
                if (delay < 10 && valid)
                    delays.add(delay);
            }

            if(delays.size() == 50) {

                int osc = (int) MathUtil.getOscillation(delays);
                delays2.add(osc);

                if(delays2.size() == 8) {
                    double stdo = MathUtil.getStandardDeviation(delays2);
                    double cps = 20.0 / MathUtil.getAverage(delays);

                    if (cps > 6.5 && stdo < 0.3D) {
                        fail(String.format("C %.2f STDO %.2f", cps, stdo), getBanVL(), 120L);
                    }

                    delays2.clear();
                }

                delays.clear();
            }

            delay = 0;
        } else if (packet instanceof FlyingEvent) {
            delay++;
        }
    }
}
