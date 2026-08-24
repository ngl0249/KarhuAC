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
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.liwk.karhu.util.MathUtil.*;

@CheckInfo(name = "AutoClicker (W)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerW extends PacketCheck {

    int flying;
    Deque<Integer> samples = new ArrayDeque<>();

    public AutoClickerW(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (!data.isHasDig() && !data.isPlacing() && !data.isUsingItem()) {

                if(data.isNewerThan8()) {
                    if (flying <= 5 && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                        samples.add(flying);
                } else {
                    if (flying <= 5)
                        samples.add(flying);
                }

                if (samples.size() >= 250) {
                    int outliers = getOutliers(samples);
                    int w = getW(samples);

                    double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(samples));

                    double ratio = getRatio(samples);

                    double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                    double product = new Product().evaluate(MathUtil.dequeTranslator(samples));
                    product *= FastMath.pow(2, -10);

                    double cps = 20.0 / average(samples);

                    if(cps > 8.5 && outliers <= 1 && w > 7 && product > 1E30 && product < 1E80 && kur < 0.5 && ratio > 10 && std > 0.4 && std < 1) {
                        if(++violations > 3) {
                            fail("* No outliers\n§f* STD §b" + format(2, std) + "\n§f* W §b" + w + "\n§f* KU §b" + kur + "\n§f* RAT §b" + ratio + "\n§f* O §b" + outliers, getBanVL(), 250L);
                        }
                    } else {
                        violations = Math.max(violations - 0.25, 0);
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

