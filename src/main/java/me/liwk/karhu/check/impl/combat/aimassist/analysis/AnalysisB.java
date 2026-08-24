package me.liwk.karhu.check.impl.combat.aimassist.analysis;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;
import org.bukkit.util.Vector;

@CheckInfo(name = "Analysis (B)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = true)
public class AnalysisB extends RotationCheck {

    private double lastAngle, lastAngleDiff;

    public AnalysisB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        if(data.getLastAttackTick() > 60 || data.getLastTarget() == -696969) {
            violations = 0;
            return;
        } else if(data.getLastAttackTick() > 40) {
            return;
        }

        EntityData edata = data.getEntityData().get(data.getLastTarget());

        if(edata == null) return;

        CustomLocation to = update.getTo();
        CustomLocation from = update.getFrom();

        float deltaPitch = Math.abs(to.getPitch() - from.getPitch());
        float deltaYaw = Math.abs(to.getYaw() - from.getYaw());

        double x = edata.getEntityBoundingBox().getCenterX(), z = edata.getEntityBoundingBox().getCenterZ();

        final double direction = MathUtil.getDirection(data.getLocation(), new Vector(x, 0.0D, z));
        final double angle = MathUtil.getAngleDistance(data.getLocation().getYaw(), direction);

        final double aDiff = Math.abs(angle - lastAngle);
        final double angleDiffDiff = Math.abs(aDiff - lastAngleDiff);

        if(deltaYaw > 3.5 && aDiff <= 0.075 && data.deltas.deltaXZ > 0.1) {
            if (++violations > 5) {
                fail("* Aimlock" +
                        "\n §f* p: §b" + deltaPitch +
                        "\n §f* y: §b" + deltaYaw +
                        "\n §f* ang: §b" + angle +
                        "\n §f* ad: §b" + aDiff +
                        "\n §f* add: §b" + angleDiffDiff, getBanVL(), 300L);
            }
        } else {
            violations = Math.max(violations - 0.15, 0);
        }

        this.lastAngle = angle;
        this.lastAngleDiff = aDiff;
    }
}
