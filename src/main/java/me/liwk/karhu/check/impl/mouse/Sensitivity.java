package me.liwk.karhu.check.impl.mouse;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.evictinglist.ConcurrentEvictingList;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.update.MovementUpdate;

import java.util.Deque;

@CheckInfo(name = "Sensitivity (A)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = false, silent = true)
public final class Sensitivity extends RotationCheck {

    public Sensitivity(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private final Deque<Float> pitchGcdList = new ConcurrentEvictingList<>(20);
    private final Deque<Float> pitchGcdList2 = new ConcurrentEvictingList<>(70);

    private float lastDeltaPitch, lastDeltaYaw;

    public float pitchMode;

    public double sensPercent;

    private int inputX, inputY;


    @Override
    public void handle(final MovementUpdate update) {

        CustomLocation to = update.getTo();
        CustomLocation from = update.getFrom();

        float deltaPitch = data.deltas.deltaPitch;
        float deltaYaw = data.deltas.deltaYaw;

        final float pitchGcd = MathUtil.getGcd(deltaPitch, lastDeltaPitch);
        final float yawGcd = MathUtil.getGcd(deltaYaw, lastDeltaYaw);

        inputX = (int) (deltaYaw / yawGcd);
        inputY = (int) (deltaPitch / this.pitchMode);


        if (data.getTeleportManager().teleportTicks > 1) {

            final int directionX = getDirectionX(update);
            final int directionY = to.getPitch() - from.getPitch() > 0 ? 1 : -1;

            data.setInputX(inputX * directionX);
            data.setInputY(inputY * directionY);

            if (deltaPitch < 4F && Math.abs(update.to.pitch) < 85) {

                //Sory papa fly cober
                if (pitchGcd > 0.009 && Math.abs(to.pitch) < 0.9 && Math.abs(from.pitch) < 0.9) {
                    this.pitchGcdList.add(pitchGcd);
                    if (this.pitchGcdList.size() == 15) {

                        this.pitchMode = MathUtil.getMode(this.pitchGcdList);

                        data.setPitchGCD(pitchMode);

                        float test1 = convertToMouseDelta(pitchMode);

                        this.sensPercent = MathHelper.floor_double(test1 * 200.0D);

                        data.setSensitivity((int) sensPercent);
                        data.setSensitivityY(test1);

                        //Bukkit.broadcastMessage("SensY1 " + test1);

                        this.pitchGcdList.clear();
                    }

                }

                //Sory papa fly cober
                /*if (pitchGcd > 0.009) {
                    this.pitchGcdList2.add(pitchGcd);
                    if (this.pitchGcdList2.size() > 60) {

                        this.pitchMode = MathUtil.getMode(this.pitchGcdList2);

                        float test1 = convertToMouseDelta(pitchMode);

                        this.sensPercent = MathHelper.floor_double(test1 * 200.0D);

                        data.setSensitivity((int) sensPercent);
                        data.setSensitivityY(test1);

                        Bukkit.broadcastMessage("SensY2 " + test1);

                        data.setSmallestRotationGCD(this.pitchMode);

                        if (this.pitchGcdList2.size() == 70) {
                            this.pitchGcdList2.clear();
                        }

                    }

                }*/
            }
        }

        lastDeltaPitch = deltaPitch;
        lastDeltaYaw = deltaYaw;
    }

    private int getDirectionX(MovementUpdate update) {
        final float lastAngle = MathUtil.trimAngle(update.from.yaw);
        final float currentAngle = MathUtil.trimAngle(update.to.yaw);
        final float dist = MathUtil.getDistanceBetweenAngles(update.from.yaw, update.to.yaw);

        final double positiveDelta = MathUtil.getDistanceBetweenAngles(lastAngle + dist, currentAngle);
        final double negativeDelta = MathUtil.getDistanceBetweenAngles(lastAngle - dist, currentAngle);

        return positiveDelta < negativeDelta ? 1 : -1;
    }

    private float convertToMouseDelta(float value) {
        // More precise inverse calculation
        float f = value / 0.15F;
        return (float) ((Math.pow(f / 8.0, 1.0/3.0) - 0.2) / 0.6);
    }
}
