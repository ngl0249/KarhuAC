package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.Bukkit;

public final class VelocityHandler {

    public static void handle(KarhuPlayer data) {

        final double check = data.getClientVersion() != null &&
                data.isNewerThan8() ? 0.003D : 0.005D;

        if (data.isTakingVertical()) {

            double velocityNew = data.getVelocityY();

            if(Math.abs(data.deltas.motionY - velocityNew) > 0.001 + (data.elapsed(data.getPredictionTicks()) < 2 ? 0.03 + 9.0E-4 : 0)) {
                velocityNew -= 0.08D;
                velocityNew *= 0.98F;
            }

            data.setVelocityY(velocityNew);

            if (Math.abs(velocityNew) < check || data.isOnGroundPacket() || data.getVelocityYTicks() >= data.getMaxVelocityYTicks() * 2) {
                data.setVelocityY(0);
                data.setConfirmingY(0);
                data.setLastVelocityYReset(data.getTotalTicks());
                data.setTakingVertical(false);
            }

        }

        if (data.getVelocityHorizontal() > 0) {

            //TODO THIS IS SOO NOT HALAL MOD

            if (data.lastAttackTick <= 1) {
                data.setVelocityX(data.getVelocityX() * 0.6D);
                data.setVelocityZ(data.getVelocityZ() * 0.6D);
            }

            float f4 = 0.91F;

            if(data.isLastOnGroundPacket()) {
                f4 = data.getCurrentFriction();
            }

            if(Math.abs(data.deltas.deltaXZ - data.getVelocityHorizontal()) > 0.001 + (data.elapsed(data.getPredictionTicks()) < 2 ? 0.03 + 9.0E-4 : 0)) {

                data.setVelocityX(data.getVelocityX() * f4);
                data.setVelocityZ(data.getVelocityZ() * f4);

                if (Math.abs(data.getVelocityX()) < check) {
                    data.setVelocityX(0);
                    data.setLastVelocityXZReset(data.getTotalTicks());
                }

                if (Math.abs(data.getVelocityZ()) < check) {
                    data.setVelocityZ(0);
                    data.setLastVelocityXZReset(data.getTotalTicks());
                }

                if (data.getVelocityXZTicks() >= data.getMaxVelocityXZTicks() * 2) { //Sorry simphon / dewgs
                    data.setVelocityX(0);
                    data.setVelocityZ(0);
                }

            }

            data.setVelocityHorizontal(MathUtil.hypot(data.getVelocityX(), data.getVelocityZ()));
        }

        data.setVelocityXZTicks(data.getVelocityXZTicks() + 1);
        data.setVelocityYTicks(data.getVelocityYTicks() + 1);

    }

}
