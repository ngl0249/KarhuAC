package me.liwk.karhu.check.impl.combat.killaura;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Killaura (M)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = true)
public final class KillauraM extends RotationCheck {

    private final Deque<Float> pitches = new LinkedList<>();

    public KillauraM(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {
        final CustomLocation to = update.getTo();
        final CustomLocation from = update.getFrom();

        final float pitch = Math.abs(to.pitch - from.pitch);
        final float yaw = Math.abs(to.yaw - from.yaw);

        if((yaw > 0.0 || pitch > 0.0) && data.getLastAttackTick() <= 1) {
            pitches.add(pitch);
        }

        if(pitches.size() == 40) {
            double avg = MathUtil.getAverage(this.pitches);
            double std = MathUtil.getStandardDeviation(this.pitches);
            double osc = MathUtil.getOscillation(this.pitches);

            if(osc > 40D && std > 12.5D && avg > 25D) {
                if(++violations > (data.getSensitivity() > 90 ? 5 : 2)) {
                    fail("* Randomized aim"
                                    + "\n §f* std: §b" + format(3, std)
                                    + "\n §f* avg: §b" + format(3, avg)
                                    + "\n §f* osc: §b" + format(3, osc),
                            getBanVL(), 300L);
                }
            } else {
                violations = Math.max(violations - (data.getSensitivity() > 90 ? 0.2 : 0.05), -0.2);
            }

            pitches.clear();
        }
    }
}
