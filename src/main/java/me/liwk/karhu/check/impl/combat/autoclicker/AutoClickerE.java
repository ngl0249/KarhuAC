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

@CheckInfo(name = "AutoClicker (E)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true, desc = "Standard consistency")
public final class AutoClickerE extends PacketCheck {

    private int flying;
    private double lastSTD;
    private Deque<Integer> samples = new ArrayDeque<>();

    private boolean lastSet;

    public AutoClickerE(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (flying < 10 && !data.isHasDig() && !data.isPlacing() && !data.isUsingItem()) samples.add(flying);
            if (samples.size() == 1000) {
                int outliers = getOutliers(samples);


                double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                double sdd = Math.abs(std - lastSTD);

                double cps = 20.0 / average(samples);

                if (std < 0.65 && sdd < 0.1) {
                    if (++violations > 1) {
                        fail("* Standard consistency" +
                                        "\n §f* STD/D: §b" + format(3, std) + "/" + format(4, sdd) +
                                        "\n §f* O: §b" + outliers +
                                        "\n §f* CPS: §b" + cps,
                                getBanVL(), 450L);
                    }
                } else {
                    decrease(0.25D);
                }

                samples.clear();
                lastSTD = std;
            }

            if(!lastSet && samples.size() == 500) {
                lastSTD = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                lastSet = true;
            }

            flying = 0;
        } else if (packet instanceof FlyingEvent) {
            if (!((FlyingEvent) packet).isTeleport()) {
                ++flying;
            }
        }
    }
}
