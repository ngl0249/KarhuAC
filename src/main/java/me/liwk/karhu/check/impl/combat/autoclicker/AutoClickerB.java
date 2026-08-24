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


@CheckInfo(name = "AutoClicker (B)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerB extends PacketCheck {

    public AutoClickerB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    int flying;
    Deque<Integer> samples = new ArrayDeque<>();

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent && !data.isHasDig()) {
            boolean valid = !data.isPlacing() && !data.isUsingItem();
            if(valid) {
                if(data.isNewerThan8()) {
                    if (flying < 10 && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                        samples.add(flying);
                } else {
                    if (flying < 10)
                        samples.add(flying);
                }
                if (samples.size() == 500) {

                    double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(samples));
                    double ske = new Skewness().evaluate(MathUtil.dequeTranslator(samples));
                    double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));

                    if (ske < 0.2 && kur < 0.0 && std < 0.7) {
                        if(++violations > 2) {
                            fail("* Weird click pattern" +
                                            "\n§f* KU §b" + format(2, kur) +
                                            "\n§f* SK §b" + format(2, ske) +
                                            "\n§f* STD §b" + format(2, std) +
                                            "\n§f* SK §b" + format(2, ske),
                                    getBanVL(), 400L);
                        }
                    } else {
                        violations = Math.max(violations - 0.5, 0);
                    }

                    samples.clear();
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
