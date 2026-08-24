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

@CheckInfo(name = "AimAssist (G)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = true)
public class AimAssistG extends RotationCheck {

    double lastDeltaYaw, lastDeltaYawAccel, lastFlagAccel;

    public AimAssistG(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        CustomLocation to = update.getTo();
        CustomLocation from = update.getFrom();

        double deltaPitch = Math.abs(to.getPitch() - from.getPitch());
        double deltaYaw = Math.abs(to.getYaw() - from.getYaw());
        final double yawAccel = Math.abs(deltaYaw - lastDeltaYaw);

        if (data.getLastAttackTick() <= 2 && !data.isPossiblyTeleporting()) {
            final double accelDiff = Math.abs(yawAccel - lastDeltaYawAccel);

            final double gcdYAW = MathUtil.getGcd(deltaYaw, lastDeltaYaw);

            final boolean invalidAim = deltaYaw > 1 && deltaYaw < 30.0 && deltaPitch < 25.0 && (yawAccel < 0.0015D);

            final double addition = gcdYAW > 0.01 ? 1.0 : 0.75;
            final double wildcard = yawAccel == lastFlagAccel ? 0.25 : 0;


            if (invalidAim) {
                violations = Math.min(violations + addition + wildcard, 10);
                if (violations > 5) {
                    fail("* Consistent rotations"
                            + "\n §f* gcdY: §b" + gcdYAW
                            + "\n §f* A: §b" + yawAccel
                            + "\n §f* AD: §b" + accelDiff, getBanVL(), 300L);
                }
                lastFlagAccel = yawAccel;
                //this.debug(new DebugValue("yA", yawAccel), new DebugValue("vl", violations));

            } else {
                violations = Math.max(violations - 0.5, 0);
            }
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaYawAccel = yawAccel;

    }


}

