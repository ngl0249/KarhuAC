package me.liwk.karhu.check.impl.combat.aimassist.analysis;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.api.debug.DebugValue;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

@CheckInfo(name = "Analysis (A)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = false)
public final class AnalysisA extends PacketCheck {

    private int attacks, swings;
    private double nearCenterHits;

    private double lastX, lastZ;

    public AnalysisA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        if(packet instanceof AttackEvent) {
            ++attacks;
        } else if(packet instanceof SwingEvent) {
            if(++swings == 100) {
                double ratio = (double) attacks / swings;
                double requiredAim = attacks / 2.5;

                if(ratio >= 0.75 && nearCenterHits >= requiredAim) {
                    fail("* Combat analysis"
                                    + "\n§f* ratio: §b" + format(3, ratio)
                                    + "\n§f* a/s: §b" + attacks + "/" + swings
                                    + "\n§f* nch: §b" + nearCenterHits,
                            getBanVL(), 300L);
                }

                attacks = swings = 0;
                nearCenterHits = 0;
            }
        } else if(packet instanceof FlyingEvent) {
            if(data.getLastAttackTick() <= 1 && data.getLastTarget() != -696969) {
                EntityData edata = data.getEntityData().get(data.getLastTarget());

                if (edata == null) return;

                double x = edata.getEntityBoundingBox().getCenterX(), z = edata.getEntityBoundingBox().getCenterZ();

                final double direction = MathUtil.getDirection(data.getLastLocation(), new Vector(x, 0.0D, z));

                final double angleNormal = MathUtil.getAngleDistance(data.getLastLocation().getYaw(), direction);
                final double angleMDFix = MathUtil.getAngleDistance(data.getLocation().getYaw(), direction);
                final double angle = Math.min(angleNormal, angleMDFix);

                final double distance = data.getBoundingBox().distance(x, z);

                final boolean movement = data.deltas.deltaXZ >= 0.08D
                        && data.deltas.deltaYaw >= 1.5F
                        && data.elapsed(data.getLastVelocityTaken()) <= 500
                        && (Math.abs(x - lastX) >= 0.0325 && Math.abs(z - lastZ) >= 0.0325);

                if(angle <= 4 * Math.max(2, distance) && movement) {
                    if(distance >= 0.4D) ++nearCenterHits;
                    else nearCenterHits += 0.55D;
                }

                this.lastX = x;
                this.lastZ = z;
            }
        }
    }
}

