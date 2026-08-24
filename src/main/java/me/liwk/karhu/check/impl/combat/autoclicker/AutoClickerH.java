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
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.liwk.karhu.util.MathUtil.*;

@CheckInfo(name = "AutoClicker (H)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerH extends PacketCheck {

    int flying;
    Deque<Integer> samples = new ArrayDeque<>();

    public AutoClickerH(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (canClick()) {
                if(data.isNewerThan8()) {
                    if (flying < 10 && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                        samples.add(flying);
                } else {
                    if (flying < 10)
                        samples.add(flying);
                }
                if (samples.size() >= 75) {

                    double entropy = getEntropy(samples);

                    if(entropy >= 0.635) {
                        clearSamples();
                        return;
                    }

                    double std = new StandardDeviation().evaluate(dequeTranslator(samples));

                    if(std >= 0.5) {
                        clearSamples();
                        return;
                    }

                    double cps = 20.0 / average(samples);

                    if (cps > 9 && entropy < 0.635 && std < 0.5 && cps != 20.0) {
                        if (++violations > 1) {
                            fail("* Low randomization" +
                                    "\n§f* §b" + String.format("std %.3f : entropy %.3f : o %s : cps %.1f",
                                    std, entropy, getOutliers(samples), cps),
                                    getBanVL(), 125L);
                        }
                    } else {
                        violations = 0;
                    }
                    clearSamples();
                }
            }
            flying = 0;
        } else if (packet instanceof FlyingEvent) {
            if(!((FlyingEvent) packet).isTeleport()) {
                ++flying;
            }
        }
    }

    private void clearSamples() {
        samples.clear();
        flying = 0;
        violations = 0;
    }
}
