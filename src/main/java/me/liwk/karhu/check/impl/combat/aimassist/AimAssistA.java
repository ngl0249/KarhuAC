package me.liwk.karhu.check.impl.combat.aimassist;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.evictinglist.EvictingList;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

import java.util.Deque;


@CheckInfo(name = "AimAssist (A)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = false)
public final class AimAssistA extends RotationCheck {

    private double lastAveragePitch, lastAverageYaw;

    private final Deque<Float> samplesP = new EvictingList<>(20),
            samplesY = new EvictingList<>(20);

    public AimAssistA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        final CustomLocation to = update.getTo();
        final CustomLocation from = update.getFrom();

        if (data.isPossiblyTeleporting() || data.getLastAttackTick() > 20) return;

        final float deltaYaw = Math.abs(to.getYaw() - from.getYaw());
        final float deltaPitch = Math.abs(to.getPitch() - from.getPitch());

        if (deltaYaw > 0.0 && deltaPitch > 0.0) {
            samplesY.add(deltaYaw);
            samplesP.add(deltaPitch);
        }

        if (samplesP.size() == 20 && samplesY.size() == 20) {

            final double averagePitch = samplesP.stream().mapToDouble(d -> d).average().orElse(0.0);
            final double averageYaw = samplesY.stream().mapToDouble(d -> d).average().orElse(0.0);

            if ((MathUtil.isNearlySame(averagePitch, lastAveragePitch, 1E-4) || MathUtil.isNearlySame(averageYaw, lastAverageYaw, 1E-4))
                    && !data.isRiding()) {
                if (++violations > 5) {
                    fail("* Consistent changes\n §f* avgPitch: §b" + averagePitch + "\n §f* avgYaw: §b" + averageYaw, getBanVL(), 300L);
                }
            } else {
                violations = Math.max(violations - 1.25, 0);
            }

            samplesP.clear();
            samplesY.clear();

            lastAverageYaw = averageYaw;
            lastAveragePitch = averagePitch;
        }

    }
}
