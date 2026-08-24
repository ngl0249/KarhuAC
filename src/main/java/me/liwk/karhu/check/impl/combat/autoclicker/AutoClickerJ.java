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
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckInfo(name = "AutoClicker (J)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerJ extends PacketCheck {

    public AutoClickerJ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private final Deque<Integer> delays = new ArrayDeque<>();
    private final Deque<Double> samples = new ArrayDeque<>();

    private int delay;

    @Override
    public void handle(final Event packet) {
        if (packet instanceof AttackEvent) {
            boolean valid = !data.isPlacing() && !data.isHasDig() && !data.isUsingItem() && data.elapsed(data.getDigTicks()) > 5;

            if(valid) {

                if (delay <= 5 && delay > 0) delays.add(delay);

                if (delays.size() >= 60) {

                    double skewness = new Skewness().evaluate(MathUtil.dequeTranslator(delays));

                    if (samples.add(skewness) && samples.size() >= 30) {

                        double avgSkewness = MathUtil.average(samples);
                        double stdSkewness = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));

                        if (avgSkewness < 0 && stdSkewness < 2 && data.getCps() > 8) {
                            if (increase(1) > 2) {

                                fail("* Bad randomization\n§f* STD §b" + stdSkewness
                                                + "\n§f* AVG §b" + avgSkewness
                                                + "\n§f* CPS §b" + data.getCps(),
                                        getBanVL(), 200L);
                            }

                        } else {
                            decrease(0.75D);
                        }
                        samples.removeFirst();
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
