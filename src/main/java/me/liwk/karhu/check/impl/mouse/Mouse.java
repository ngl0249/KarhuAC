package me.liwk.karhu.check.impl.mouse;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;
import org.bukkit.Bukkit;

@CheckInfo(name = "Cinematic (A)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = false, silent = true)
public final class Mouse extends RotationCheck {

    private double deltaX, deltaY, mouseX, mouseY;

    private float lastPitch, lastPitchAccel, lastYaw, lastYawAccel;
    private int ticks;

    public Mouse(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        CustomLocation to = update.getTo();
        CustomLocation from = update.getFrom();

        deltaX = Math.abs(to.getYaw() - from.getYaw());
        deltaY = Math.abs(to.getPitch() - from.getPitch());

        double deltaYAccel = Math.abs(deltaY - lastPitch);
        double deltaXAccel = Math.abs(deltaX - lastYaw);

        mouseX = from.getYaw();
        mouseY = from.getPitch();

        float f = data.getSensitivity() * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;
        float f2 = (float)mouseX * f1;
        float f3 = (float)mouseY * f1;
        byte b0 = 1;

        float[] angles = getAngles(f2, f3 * (float)b0);

        data.setPredictYaw(angles[0]);
        data.setPredictPitch(angles[1]);

        if((isNearlySame(deltaY, lastPitch)
                || isNearlySame(deltaYAccel, lastPitchAccel))
                || isNearlySame(deltaX, deltaXAccel)
                || isNearlySame(deltaXAccel, lastYawAccel)) {
            ticks = Math.min(80, ticks + 1);
            if(ticks >= 3) {
                data.setCinematic(true);
                data.setLastCinematic(data.getTotalTicks());
            }
        } else {
            ticks = Math.max(ticks - 1, 0);
            if(ticks <= 1) {
                data.setCinematic(false);
            }
        }
        lastPitch = (float) deltaY;
        lastPitchAccel = (float) deltaYAccel;
        lastYaw = (float) deltaX;
        lastYawAccel = (float) deltaXAccel;
    }

    public boolean isNearlySame(double d1, double d2) {
        double max = data.getSensitivity() >= 100 ? (0.0425 * (data.getSensitivityY() * 3.1))
                : data.getSensitivity() >= 160 ? (0.07 * (data.getSensitivityY() * 3.2)) : 0.0325;

        if(data.getSensitivity() >= 160 && Math.abs(d1 - d2) > 1 && Math.abs(d1 - d2) < 8) {
            return true;
        }
        return Math.abs(d1 - d2) < max && Math.abs(d1 - d2) > 0.0015;
    }


    public float[] getAngles(float yaw, float pitch) {

        float yaw2 = (float) mouseX, pitch2 = (float) mouseY;

        yaw2 = (float)((double)yaw2 + (double)yaw * 0.15D);
        pitch2 = (float)((double)pitch2 - (double)pitch * 0.15D);

        return new float[] { yaw2, pitch2 };
    }
}
