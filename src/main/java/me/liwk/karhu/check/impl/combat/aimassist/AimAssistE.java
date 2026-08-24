package me.liwk.karhu.check.impl.combat.aimassist;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "AimAssist (E)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = false)
public class AimAssistE extends RotationCheck {

    private float lastDeltaPitch, lastDeltaYaw;
    private float lastGCD;

    public AimAssistE(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        final CustomLocation to = update.to;
        final CustomLocation from = update.from;

        final float deltaPitch = Math.abs(to.getPitch() - from.getPitch());
        final float deltaYaw = Math.abs(to.getYaw() - from.getYaw());

        boolean canCheck = data.elapsed(data.getLastCinematic()) > 5
                && Math.abs(to.pitch) < 90f
                && Math.abs(from.pitch) < 90f
                && deltaPitch <= 5f
                && !data.recentlyTeleported(5);

        final double addition = lastGCD < 0.003 ? 0.5 : 0;

        //double gcd = MathUtil.getGcdTest(0.07599158585071564, 0.07599158585071564);
        if (data.getLastAttackTick() < 3 || data.elapsed(data.getPlaceTicks()) <= 4) {
            if (canCheck) {
                float gcdPITCH = MathUtil.getGcd(deltaPitch, lastDeltaPitch);
                float gcdYAW = MathUtil.getGcd(deltaYaw, lastDeltaYaw);

                if (deltaPitch > 0.2 && Math.abs(deltaPitch - lastDeltaPitch) > 0.2 && gcdPITCH < 0.008) {

                    violations = Math.min(30, violations + 0.5 + addition); //Prevent buffer overflow

                    if (violations > 17.5) {
                        fail("* Consistent rotations" +
                                "\n §f* gcd: §b" + gcdPITCH + " | " + gcdYAW +
                                "\n §f* deltaPitch: §b" + deltaPitch, getBanVL(), 300);
                    }

                    if (violations > 5) {
                        data.setReduceNextDamage(true);
                    }
                } else {
                    violations = Math.max(violations - 0.65, 0);
                }
                lastGCD = gcdPITCH;
            } else violations = Math.max(violations - 1.1, 0);
        }

        lastDeltaPitch = deltaPitch;
        lastDeltaYaw = deltaYaw;
    }
}

