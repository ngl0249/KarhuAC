package me.liwk.karhu.check.impl.combat.aimassist;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "AimAssist (I)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = false)
public class AimAssistI extends RotationCheck {

    private int zeroDeltaTicks;

    public AimAssistI(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        if (data.getLastAttackTick() >= 3 || data.isPossiblyTeleporting()) return;

        final CustomLocation to = update.getTo();
        final CustomLocation from = update.getFrom();

        final float deltaYaw = Math.abs(to.getYaw() - from.getYaw());
        final float deltaPitch = Math.abs(to.getPitch() - from.getPitch());

        if (deltaPitch == 0) {
            ++zeroDeltaTicks;
        } else {
            zeroDeltaTicks = 0;
        }

        if (zeroDeltaTicks > 40 && deltaYaw > 3 && Math.abs(to.getPitch()) < 45 && data.deltas.deltaXZ > 0.08) {
            if (++violations > 5) {
                fail("* Weird rotation\n §f* p: §b" + to.getPitch() + "\n §f* lp: §b" + from.getPitch(), getBanVL(), 300L);
            }
        } else {
            violations *= 0.75D;
        }
    }
}
