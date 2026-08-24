package me.liwk.karhu.check.impl.movement.speed;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.impl.combat.killaura.KillauraE;
import me.liwk.karhu.check.impl.movement.omnisprint.OmniSprintA;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;
import org.bukkit.util.Vector;

@CheckInfo(name = "Speed (C)", category = Category.MOVEMENT, subCategory = SubCategory.SPEED, experimental = false)
public final class SpeedC extends PositionCheck {

    private double bucketVl, sprintVl;

    public SpeedC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate e) {

        Vector velocity = data.getTickedVelocity();

        if (data.deltas.deltaXZ < data.getAttributeSpeed()
                || data.deltas.lastDXZ < data.offsetMove() + 0.01
                || data.isOnLadder()
                || velocity != null
                || data.elapsed(data.getLastPistonPush()) <= 3
                || data.elapsed(data.getLastFlyTick()) <= 30
                || data.elapsed(data.getLastGlide()) <= 15
                || data.elapsed(data.getLastRiptide()) <= 15
                || data.isInBed()
                || data.isOnScaffolding()
                || data.isLastInBed()
                || data.elapsed(data.getLastCollidedH()) <= 2
                || data.elapsed(data.getLastCollidedGhost()) <= 2) {
            decrease(0.005D);
            bucketVl = Math.max(0, bucketVl - 0.2);
            sprintVl = Math.max(0, sprintVl - 0.3);
            return;
        }

        final double threshold = 0.003;
        final double leniency = getLeniency(threshold);
        double predicted = simulation.getOutputXZ();

        double tMult = Karhu.getInstance().getConfigManager().getSpeedCMult();
        double diff = data.deltas.deltaXZ - predicted;

        checkKeepSprint(diff);
        checkOmniSprint(diff, leniency, predicted);

        if (bucketVl > 10) {
            disallowMove(false);
        }

        if (sprintVl > 15) {
            disallowMove(false);
        }

        if (simulation.getLowestMatch() > leniency) {

            if (increase(1) > 6) {
                fail("* Prediction"
                                + "\n §f* match §b" + simulation.getLowestMatch()
                                + "\n §f* predict §b" + predicted + " | " + simulation.getAttributeSpeed()
                                + "\n §f* diff §b" + diff
                                + "\n §f* len §b" + leniency
                                + "\n §f* vel | p | m §b" + data.elapsed(data.getLastVelocityTaken()) + " | " + data.elapsed(data.getLastPushedByWater()) + " | " + data.getMoveTicks()
                                + "\n §f* s §b" + simulation.isSprinting() + " u " + simulation.isUseItem()
                                + "\n §f* move §b" + data.deltas.deltaXZ,
                        300);
                violations = 6;
            }
            debug(String.format("match: %s diff: %s §fbuffer: %.1f p: %s", simulation.getLowestMatch(), diff, violations, data.elapsed(data.getLastPushedByWater())));
        } else decrease(0.075D);

        bucketVl = Math.max(0, bucketVl - 0.2);
        sprintVl = Math.max(0, sprintVl - 0.3);
    }

    private double getLeniency(double threshold) {
        double leniency = threshold;

        boolean entity = data.elapsed(data.getLastCollidedWithEntity()) <= 2;

        boolean waterPush = data.elapsed(data.getLastPushedByWater()) <= 4;
        int lastInLiquid = data.elapsed(data.getLastInLiquid());

        //Bit vogue, but the way our simulation works it can cause bigger offset than 0.03 :/
        if (data.getMoveTicks() <= 3) leniency += (data.offsetMove() + data.clamp()) * 2;

        if (data.elapsed(simulation.getEdgeSneakTick()) <= 3) leniency += 0.15; //give leniency
        if (data.elapsed(data.getLastOnSoul()) <= 3) leniency += 0.05; //give leniency
        if (data.elapsed(data.getLastOnSlime()) <= 3) leniency += 0.05; //give leniency
        if (data.elapsed(data.getLastInBerry()) <= 3) leniency += 0.05; //give leniency
        if (data.isOnHoney() || data.isWasOnHoney()) leniency += 0.05; //give leniency

        if (waterPush) leniency += 0.02; //give leniency

        if (data.elapsed(data.getBucketTicks()) <= data.getPingInTicks() + 3) {
            leniency += 0.12;
            ++bucketVl;
        }

        if (data.isNewerThan12() && !data.isWasOnWater() && data.isWasWasOnWater() && !waterPush) {
            leniency += 0.05;
        }

        if (data.isNewerThan12() && !data.isOnWater() && data.isWasOnWater() && !waterPush) {
            leniency += 0.005;
        }

        if (data.isNewerThan12() && data.elapsed(data.getLastInLiquid()) <= 1) {
            leniency += 0.02;
        }

        if (!data.isWasOnWater() && lastInLiquid > 2 && lastInLiquid <= 5 + Math.min(15, data.getPingInTicks())) {
            leniency += 0.05;
        }
        /*
        Fix this later, but leaving for army no time to research what causes this
        */
        /*if (data.isSprinting() != data.isWasSprinting()) {
            leniency += 0.01;
            ++sprintVl;
        }*/

        if (data.elapsed(data.getLastInGhostLiquid()) <= 2) leniency += 0.1;
        if (data.isInWeb() || data.isWasInWeb()) leniency += 0.25; //give leniency
        if (data.elapsed(data.getLastInPowder()) <= 3) leniency += 0.25; //give leniency
        if (data.elapsed(data.getLastCollidedGhost()) <= 2) leniency += 0.3; //give leniency
        if (entity) leniency += 0.055D;

        //Silent moment for this piece of art :pray:
        if (data.elapsed(data.getFrictionUncertain()) <= data.getPingInTicks() + 1) leniency += 0.2;

        leniency += Math.max(data.getRodPullLeniencyXZ(), data.getLastRodPullLeniencyXZ());
        return leniency;
    }

    private void checkKeepSprint(double diff) {
        if (Karhu.getInstance().getConfigManager().isCheckKeepSprint()) {
            if (!simulation.isAttacking() && data.getLastAttackTick() <= 1
                    && data.getLastTarget() != -696969
                    && simulation.isSprinting()
                    && simulation.getMoveForward() > 0
                    && data.elapsed(data.getLastInLiquid()) > 1
                    && data.elapsed(data.getYawFucked()) > 1
                    && !data.isNewerThan8()
                    && data.deltas.deltaXZ > simulation.getAttributeSpeed() + data.offsetMove()
                    && data.deltas.lastDXZ > simulation.getAttributeSpeed() + data.offsetMove()) {
                if (increaseSub(1D) > 3) {
                    data.getCheckManager().getCheck(KillauraE.class)
                            .fail("* KeepSprint"
                                            + "\n §f* match §b" + simulation.getLowestMatch()
                                            + "\n §f* diff §b" + diff
                                            + "\n §f* move §b" + data.deltas.deltaXZ,
                                    300);
                    subVl = 2;
                }
            }
            decreaseSub(0.2D);
        }
    }

    private void checkOmniSprint(double diff, double leniency, double predicted) {
        if (simulation.getMoveForward() <= 0
                && data.isSprinting()
                && !data.isSettingMetadataSprint()
                && !data.isMetadataSprint()
                && !data.isInvalidSprint()
                && data.getLastAttackTick() > 4
                && data.deltas.deltaXZ > simulation.getAttributeSpeed()
                && data.elapsed(data.getLastInLiquid()) > 1) {
            if (increaseSub(0.5) > 3) {
                data.getCheckManager().getCheck(OmniSprintA.class)
                        .fail("* Illegal sprint direction"
                                        + "\n §f* match §b" + simulation.getLowestMatch()
                                        + "\n §f* predict §b" + predicted
                                        + "\n §f* diff §b" + diff
                                        + "\n §f* len §b" + leniency
                                        + "\n §f* moveSpeed §b" + simulation.getAttributeSpeed()
                                        + "\n §f* vel §b" + data.elapsed(data.getLastVelocityTaken())
                                        + "\n §f* p §b" + data.elapsed(data.getLastPushedByWater())
                                        + "\n §f* move §b" + data.deltas.deltaXZ,
                                300);
            }
        } else {
            decreaseSub(0.05);
        }
    }
}
