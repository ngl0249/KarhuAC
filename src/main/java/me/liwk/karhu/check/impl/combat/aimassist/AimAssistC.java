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

@CheckInfo(name = "AimAssist (C)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = true, credits = "§c§lCREDITS: §aWizzard §7made this check.")
public class AimAssistC extends RotationCheck {

    double pitch, yaw;
    double thresholdP, thresholdY;

    public AimAssistC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        CustomLocation to = update.getTo();
        CustomLocation from = update.getFrom();

        float pitch = Math.abs(to.getPitch() - from.getPitch());
        float yaw = Math.abs(to.getYaw() - from.getYaw());

        if (!data.isPossiblyTeleporting() && !data.isRiding() && !data.recentlyTeleported(3) && data.getSensitivity() < 150) {

            double gcdP = MathUtil.getGcd(pitch, this.pitch);
            double gcdY = MathUtil.getGcd(yaw, this.yaw);

            if (gcdP > 0.7 && (pitch % this.pitch == 0 || Double.isNaN(pitch % this.pitch)) && pitch <= 10f) {
                thresholdP = Math.min(10, thresholdP + 0.5);
                if (thresholdP > 8) {
                    fail("* Vertical aimassist\n §f* GCD: §b" + gcdP + "\n §f* COUNT: §b" + thresholdP, getBanVL(), 150L);
                }
            } else {
                thresholdP = Math.max(0, thresholdP - 1.25);
            }

            if (gcdY > 0.7 && (yaw % this.yaw == 0 || Double.isNaN(yaw % this.yaw)) && yaw <= 10f) {
                thresholdY = Math.min(10, thresholdY + 0.5);
                if (thresholdY > 4) {
                    fail("* Horizontal aimassist\n §f* GCD: §b" + gcdY + "\n §f* COUNT: §b" + thresholdY, getBanVL(), 150L);
                }
            } else {
                thresholdY = Math.max(0, thresholdY - 1.5);
            }
        }

        this.pitch = pitch;
        this.yaw = yaw;
    }
}
