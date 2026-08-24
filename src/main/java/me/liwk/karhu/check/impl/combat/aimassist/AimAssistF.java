package me.liwk.karhu.check.impl.combat.aimassist;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;
import org.bukkit.Bukkit;

@CheckInfo(name = "AimAssist (F)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = true)
public class AimAssistF extends RotationCheck {

    private float lastDeltaPitch, lastDeltaYaw;
    private int streak;

    public AimAssistF(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        final CustomLocation to = update.getTo();
        final CustomLocation from = update.getFrom();

        final float deltaPitch = Math.abs(to.getPitch() - from.getPitch());
        final float deltaYaw = Math.abs(to.getYaw() - from.getYaw());

        if (data.getLastAttackTick() <= 5) {
            if (deltaYaw > 0.001D && deltaYaw <= 5.0F && lastDeltaYaw <= 5.0F && Math.abs(to.pitch) <= 80) {
                double gcdYAW = MathUtil.getGcd(deltaYaw, lastDeltaYaw);
                if (gcdYAW < 0.009 && !data.isCinematic()) {

                    double gcdPITCH = MathUtil.getGcd(deltaPitch, lastDeltaPitch);

                    if (deltaPitch > 0 && gcdPITCH < 0.009) {
                        streak = 0;
                        violations = 0;
                    }
                    if (++streak > 20 && lastDeltaPitch == 0 && ++violations > 15) {
                        fail("* Consistent rotations\n §f* gcdY: §b" + gcdYAW + "\n §f* gcdP: §b" + gcdPITCH, getBanVL(), 300L);
                        violations = 0;
                    }
                } else {
                    decrease(0.5);
                }
            }
        }
        lastDeltaPitch = deltaPitch;
        lastDeltaYaw = deltaYaw;
    }
}
