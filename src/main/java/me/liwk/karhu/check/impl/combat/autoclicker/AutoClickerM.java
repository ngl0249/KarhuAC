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
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.liwk.karhu.util.MathUtil.*;

@CheckInfo(name = "AutoClicker (M)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerM extends PacketCheck {

    int flying;
    double lastSTD;
    Deque<Integer> samples = new ArrayDeque<>();

    public AutoClickerM(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (!data.isHasDig() && !data.isPlacing() && !data.isUsingItem() && data.elapsed(data.getDigTicks()) > 5) {
                if (data.isNewerThan8()) {
                    if (flying <= 5 && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                        samples.add(flying);
                } else {
                    if (flying <= 5)
                        samples.add(flying);
                }
                if (samples.size() == 300) {

                    double avgDifference = computeAverageDifference(samples);

                    double cps = 20.0 / average(samples);
                    double std = new StandardDeviation().evaluate(dequeTranslator(samples));

                    double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(samples));

                    if (kur < 0 && avgDifference > 0.55 && avgDifference <= 1) {
                        if (increase(Math.abs(kur)) > 1) {
                            fail("* Generic\n§f* §b" + String.format("std %.3f : ad %.3f : ku %s : cps %.1f", std, avgDifference, kur, cps), getBanVL(), 600L);
                        }
                    } else {
                        decrease(0.15);
                    }

                    samples.clear();
                    lastSTD = std;
                }
            }
            flying = 0;
        } else if (packet instanceof FlyingEvent) {
            if(!((FlyingEvent) packet).isTeleport()) {
                ++flying;
            }
        }
    }
}
