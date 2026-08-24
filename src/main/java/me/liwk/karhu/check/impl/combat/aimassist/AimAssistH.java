package me.liwk.karhu.check.impl.combat.aimassist;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "AimAssist (H)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = false)
public class AimAssistH extends RotationCheck {
    public AimAssistH(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        if (data.getLastAttackTick() >= 20 || data.isPossiblyTeleporting()) return;

        final CustomLocation to = update.getTo();
        final CustomLocation from = update.getFrom();

        final float deltaYaw = Math.abs(to.getYaw() - from.getYaw());

        if (to.getPitch() == 0 && from.getPitch() == 0 && deltaYaw > 2) {
            if (++violations > 3) {
                fail("* Weird rotation\n §f* p: §b" + to.getPitch() + "\n §f* lp: §b" + from.getPitch(), getBanVL(), 300L);
            }
        } else {
            violations *= 0.8;
        }
    }
}
