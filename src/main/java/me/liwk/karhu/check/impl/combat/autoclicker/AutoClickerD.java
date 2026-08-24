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

@CheckInfo(name = "AutoClicker (D)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerD extends PacketCheck {

    int flying;
    double lastSTD;
    Deque<Integer> samples = new ArrayDeque<>();

    public AutoClickerD(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (flying < 10 && !data.isHasDig() && !data.isPlacing() && !data.isUsingItem()) samples.add(flying);
            if (samples.size() == 1000) {
                int outliers = getOutliers(samples);


                double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(samples));
                double sdd = Math.abs(std - lastSTD);

                double cps = 20.0 / average(samples);

                if (std < 0.8 && kur < 0.5 && sdd < 0.04 && outliers <= 6) {
                    if (++violations > 1) {
                        fail("* Repeating pattern" +
                                        "\n §f* O: §b" + outliers +
                                        "\n §f* CPS: §b" + cps,
                                getBanVL(), 450L);
                    }
                } else {
                    decrease(0.5D);
                }

                samples.clear();
                lastSTD = std;

            }

            flying = 0;
        } else if (packet instanceof FlyingEvent) {
            if (!((FlyingEvent) packet).isTeleport()) {
                ++flying;
            }
        }
    }
}
