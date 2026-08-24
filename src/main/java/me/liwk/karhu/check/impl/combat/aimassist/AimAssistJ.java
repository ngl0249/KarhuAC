package me.liwk.karhu.check.impl.combat.aimassist;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "AimAssist (J)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = true, credits = "§c§lCREDITS: §aWizzard §7made this check.")
public class AimAssistJ extends RotationCheck {

    public AimAssistJ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        final CustomLocation to = update.getTo();

        if (data.getSensitivityY() != -1 && data.getLastAttackTick() <= 1 && !data.recentlyTeleported(5)) {

            final float fixedYaw = fixedSensitivity(data.getSensitivityY(), to.yaw);
            final float fixedPitch = fixedSensitivity(data.getSensitivityY(), to.pitch);

            final float diffYaw = Math.abs(to.yaw - fixedYaw);
            final float diffPitch = Math.abs(to.pitch - fixedPitch);

            if (diffYaw == 0 || diffPitch == 0) {
                if (++violations > 10D) {
                    fail("* Round gcd patch\n §f* diffYaw: §b" + diffYaw + "\n §f* diffPitch: §b" + diffPitch, getBanVL(), 300L);
                }
            } else decrease(0.75D);
        }

    }

    private float fixedSensitivity(float sensitivity, float angle) {
        float f = sensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 1.2F;

        //Bukkit.broadcastMessage("angle modulo: " + (angle % gcd));

        return angle - (angle % gcd);
    }
}
