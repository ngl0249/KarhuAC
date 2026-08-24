package me.liwk.karhu.check.impl.combat.autoclicker;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
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
import org.apache.commons.math3.stat.descriptive.summary.Product;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.liwk.karhu.util.MathUtil.*;

@CheckInfo(name = "AutoClicker (U)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerU extends PacketCheck {

    int flying;
    double lastSTD;
    Deque<Integer> samples = new ArrayDeque<>();

    public AutoClickerU(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (!data.isHasDig() && !data.isPlacing() && !data.isUsingItem() && data.elapsed(data.getDigTicks()) > 5) {
                if(data.isNewerThan8()) {
                    if (flying < 10 && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                        samples.add(flying);
                } else {
                    if (flying < 10)
                        samples.add(flying);
                }
                if (samples.size() == 300) {

                    int outliers = getOutliers(samples);

                    double cps = 20.0 / average(samples);
                    double std = new StandardDeviation().evaluate(dequeTranslator(samples));

                    double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(samples));
                    double ske = new Skewness().evaluate(MathUtil.dequeTranslator(samples));

                    if (kur < 0 && ske < -0.5 && outliers <= 3) {
                        fail("* Weird randomization\n§f* §b" + String.format("std %.3f : sk %.3f : o %s : ku %s : cps %.1f", std, ske, outliers, kur, cps), getBanVL(), 600L);
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
