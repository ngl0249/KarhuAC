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
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.liwk.karhu.util.MathUtil.*;

@CheckInfo(name = "AutoClicker (O)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerO extends PacketCheck {

    int flying;
    double lastSTD;
    Deque<Integer> samples = new ArrayDeque<>();
    Deque<Double> samples2 = new ArrayDeque<>();

    public AutoClickerO(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (flying <= 5 && !data.isHasDig() && !data.isPlacing() && !data.isUsingItem()) samples.add(flying);
            if (samples.size() == 100) {

                double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));

                if (lastSTD != 0) {

                    double sdd = Math.abs(std - lastSTD);

                    samples2.add(sdd);

                    if (samples2.size() == 3) {
                        double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(samples));
                        double ske = new Skewness().evaluate(MathUtil.dequeTranslator(samples));

                        double avgSdd = MathUtil.getAverage(samples2);

                        double cps = 20.0 / average(samples);

                        if (avgSdd < 0.1 && kur > 1 && ske > 1 && std > 0.4 && std < 0.9) {
                            if (increase(Math.abs(1 + avgSdd)) > 1.5) {
                                fail("* Generic\n§f* §b" + String.format("std %.3f : asdd %.3f : ku %s : cps %.1f", std, avgSdd, kur, cps), getBanVL(), 600L);
                            }
                        } else {
                            decrease(0.45);
                        }
                        MathUtil.removeOldestItems(samples2, 2);
                    }
                }

                samples.clear();
                lastSTD = std;

            }

            flying = 0;
        } else if (packet instanceof FlyingEvent) {
            if(!((FlyingEvent) packet).isTeleport()) {
                ++flying;
            }
        }
    }
}
