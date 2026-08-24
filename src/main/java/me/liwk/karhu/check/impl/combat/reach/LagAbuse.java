package me.liwk.karhu.check.impl.combat.reach;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.data.combat.CombatData;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * LagAbuse Check - Detects backtrack maybe
 */
@CheckInfo(name = "LagAbuse", category = Category.COMBAT, subCategory = SubCategory.REACH, experimental = false)
public final class LagAbuse extends PacketCheck {
    private static final int MAX_COMBAT_DATA_SIZE = 40;
    private static final int ATTACK_TICK_THRESHOLD = 50;
    private static final double PING_DIFFERENCE_THRESHOLD = 50.0;

    private boolean specialCase;

    public LagAbuse(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (!(packet instanceof FlyingEvent)) return;

        List<CombatData> combatDataClose = data.getCombatDataClose();
        List<CombatData> combatDataFar = data.getCombatDataFar();

        // Manage combat data size and age
        manageCombatData(combatDataClose, combatDataFar, ((FlyingEvent) packet).getCurrentTimeMillis());

        // Check for lag abuse conditions
        if (canCheckCombat(combatDataClose, combatDataFar)) {
            handleLagAbuse(combatDataClose, combatDataFar);
        }
    }

    private void manageCombatData(List<CombatData> combatDataClose, List<CombatData> combatDataFar, long currentTime) {
        // Clear outdated data
        combatDataClose.removeIf(data -> currentTime - data.getTimestamp() > 8000L);
        combatDataFar.removeIf(data -> currentTime - data.getTimestamp() > 8000L);

        // Maintain maximum size
        if (combatDataClose.size() > MAX_COMBAT_DATA_SIZE) {
            combatDataClose.remove(0);
        }

        if (combatDataFar.size() > MAX_COMBAT_DATA_SIZE) {
            combatDataFar.remove(0);
        }
    }

    private boolean canCheckCombat(List<CombatData> combatDataClose, List<CombatData> combatDataFar) {
        return data.getLastAttackTick() <= ATTACK_TICK_THRESHOLD &&
                combatDataClose.size() >= 30 && combatDataFar.size() >= 30;
    }

    private void handleLagAbuse(List<CombatData> combatDataClose, List<CombatData> combatDataFar) {
        double[] averages = calculateAverages(combatDataClose, combatDataFar);

        double avgClientDist = averages[0];
        double avgServerDist = averages[1];
        double avgPing = averages[2];
        double avgPingClose = averages[3];
        double avgPingFar = averages[4];

        double pingDifference = avgPingClose - avgPingFar;
        double pingDifferenceNormal = avgPingClose - avgPing;

        if (isLagAbuseConditionMet(avgClientDist, avgServerDist, avgPing,
                avgPingClose, avgPingFar,
                pingDifference, pingDifferenceNormal)) {
            double increase = Math.min(3, Math.round(pingDifference) / 100.0);
            if (increase(increase) > 3) {

                data.setHitsToCancel((int) (data.getHitsToCancel() + Math.round(increase)));

                MiscellaneousAlertPoster.postMitigation(data, violations,
                        "LagAbuse",
                        "* Artificial lag" +
                                "\n * difference §b" + pingDifferenceNormal
                );
            } else {
                data.setHitsToCancel(data.getHitsToCancel() + 1);
                data.setCancelLagAbuseHits(true);
                specialCase = true;

                MiscellaneousAlertPoster.postMitigation(data, violations,
                        "LagAbuse",
                        "* Artificial lag" +
                                "\n * difference §b" + pingDifferenceNormal +
                                "\n * X §b" + avgPing +
                                "\n * Y §b" + avgPingClose +
                                "\n * Z §b" + avgPingFar
                );
            }

            MathUtil.removeOldestItems(combatDataClose, 10);
            MathUtil.removeOldestItems(combatDataFar, 10);
        } else {
            decrease(0.005);
        }

        if (!specialCase) {
            data.setCancelLagAbuseHits(violations > 5);
        }
        specialCase = false;
    }

    private boolean isLagAbuseConditionMet(
            double avgClientDist, double avgServerDist,
            double avgPing, double avgPingClose,
            double avgPingFar, double pingDifference,
            double pingDifferenceNormal
    ) {

        //Debug
        //Bukkit.broadcastMessage("AP " + avgPing + " APC " + avgPingClose + " APF " + avgPingFar);

        return avgClientDist > avgServerDist &&
                avgPingClose > avgPing &&
                avgPingClose > avgPingFar * 1.5 &&
                pingDifferenceNormal >= PING_DIFFERENCE_THRESHOLD &&
                pingDifference >= PING_DIFFERENCE_THRESHOLD;
    }

    private double[] calculateAverages(List<CombatData> combatDataClose, List<CombatData> combatDataFar) {
        List<CombatData> combinedData = new ArrayList<>(combatDataClose);
        combinedData.addAll(combatDataFar);

        return new double[]{
                combinedData.stream()
                        .mapToDouble(CombatData::getNoninterpolatedDist)
                        .average()
                        .orElse(0.0),

                combinedData.stream()
                        .mapToDouble(CombatData::getInterpolatedDist)
                        .average()
                        .orElse(0.0),

                combinedData.stream()
                        .mapToLong(CombatData::getPing)
                        .average()
                        .orElse(0.0),

                combatDataClose.stream()
                        .mapToLong(CombatData::getPing)
                        .average()
                        .orElse(0),

                combatDataFar.stream()
                        .mapToLong(CombatData::getPing)
                        .average()
                        .orElse(0)
        };
    }
}