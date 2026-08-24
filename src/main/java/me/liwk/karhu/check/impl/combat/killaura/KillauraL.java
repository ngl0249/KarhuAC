package me.liwk.karhu.check.impl.combat.killaura;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

@CheckInfo(name = "Killaura (L)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = true)
public final class KillauraL extends RotationCheck {

    private final Deque<Float> pitches = new LinkedList<>();
    private final Deque<Float> yaws = new LinkedList<>();

    public KillauraL(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {
        /*final CustomLocation to = update.getTo();
        final CustomLocation from = update.getFrom();

        final float pitch = Math.abs(to.pitch - from.pitch);
        final float yaw = Math.abs(to.yaw - from.yaw);

        if((yaw > 0.0 || pitch > 0.0) && data.getLastAttackTick() <= 1) {
            pitches.add(pitch);
            yaws.add(yaw);
        }

        if (pitches.size() == 40 && yaws.size() == 40) {
            double avgPitches = MathUtil.getAverage(this.pitches);
            double avgYaws = MathUtil.getAverage(this.yaws);

            double correlation = calculateCorrelation(pitches, yaws);

            double difference = Math.abs(avgYaws - avgPitches);

            double increaseStandard = correlation < 0 ? 1 : 0.7;

            if (correlation < 0.2 && difference < 0.8 && avgPitches > 1 && avgYaws > 1) {

                if (increase(increaseStandard + Math.abs(correlation)) > 2) {
                    fail(String.format("C: %.2f P: %.2f Y: %.2f VL:  %.2f",
                            correlation, avgPitches, avgYaws, violations),
                            500L);
                }

            } else {
                decrease((0.1 - Math.abs(correlation) / 10));
            }


            pitches.clear();
            yaws.clear();
        }

        if (pitches.size() >= 40) {
            pitches.removeFirst();
        }

        if (yaws.size() >= 40) {
            pitches.removeFirst();
        }*/
    }


    private double calculateCorrelation(Deque<Float> x, Deque<Float> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Deques must be of the same size and non-empty");
        }

        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0;
        double sumXSquare = 0, sumYSquare = 0;

        // Declare iterators explicitly
        Iterator<Float> xIter = x.iterator();
        Iterator<Float> yIter = y.iterator();

        while (xIter.hasNext() && yIter.hasNext()) {
            double xi = xIter.next();
            double yi = yIter.next();

            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumXSquare += xi * xi;
            sumYSquare += yi * yi;
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumXSquare - sumX * sumX) * (n * sumYSquare - sumY * sumY));

        return (denominator == 0) ? 0 : numerator / denominator;
    }
}