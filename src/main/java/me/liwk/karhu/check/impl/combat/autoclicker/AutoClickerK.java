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
import org.apache.commons.math3.stat.descriptive.moment.SemiVariance;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckInfo(name = "AutoClicker (K)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerK extends PacketCheck {

    private int flying;
    private Deque<Integer> samples = new ArrayDeque<>();

    public AutoClickerK(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (data.isNewerThan8()) {
                if (flying < 8 && canClick()
                        && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                    samples.add(flying);
            } else {
                if (flying < 8 && canClick())
                    samples.add(flying);
            }

            if (samples.size() == 500) {
                double std = MathUtil.getStandardDeviation(samples);
                double cps = 20.0 / MathUtil.getAverage(samples);
                double semiVar = new SemiVariance().evaluate(MathUtil.dequeTranslator(samples));

                double divided = semiVar / std;

                if (cps > 8 && divided < 0.06 && std < 0.75) {
                    if (increase(1) > 1) {
                        fail("* Low variation" +
                                        "\n §f* STD: §b" + std +
                                        "\n §f* DIVIDED: §b" + divided +
                                        "\n §f* SEMIV: §b" + semiVar +
                                        "\n §f* CPS: §b" + cps,
                                getBanVL(), 450L);
                    }
                } else {
                    decrease(0.25D);
                }

                samples.clear();
            }

            flying = 0;
        } else if (packet instanceof FlyingEvent) {
            if (!((FlyingEvent) packet).isTeleport()) {
                ++flying;
            }
        }
    }
}
