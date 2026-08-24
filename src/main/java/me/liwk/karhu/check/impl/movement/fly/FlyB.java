package me.liwk.karhu.check.impl.movement.fly;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Fly (B)", category = Category.MOVEMENT, subCategory = SubCategory.FLY, experimental = false)
public final class FlyB extends PositionCheck {

    private float slimeJump;

    public FlyB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        double limit = 0.0;

        if (this.data.isOnSlime() && this.data.deltas.lastMotionY < -0.4D) {
            slimeJump = data.fallDistance;
        } else {
            if ((data.getDeltas().motionY <= 1E-7 && data.getAirTicks() > 4) || data.getAirTicks() > 80) {
                slimeJump = 0;
            }
        }

        limit += (data.getLevitationLevel() * 0.5f);
        limit += Math.abs(slimeJump);

        if (this.data.isInUnloadedChunk()) {
            if (this.data.getLocation().getY() > 0.0D) {
                limit = 0.1D;
            } else {
                limit = 0.0D;
            }
        }

        if(data.elapsed(data.getLastInLiquid()) < 15 && Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8)) {
            limit = 5.0D;
        }

        double maxVL = 7.5D;

        final double accel = Math.abs(data.deltas.lastMotionY - data.deltas.motionY);

        int jumpTicks = (int) (data.getJumpBoost() > 0 ? data.getJumpBoost() * 10.6F : 0);

        final boolean valid = !this.data.isTakingVertical()
                && this.data.getLocation().y > -100
                && data.elapsed(data.getLastOnClimbable()) > 35
                && !data.isNearClimbable()
                && data.elapsed(data.getLastVelocityYReset()) > 2
                && !this.data.isOnWeb()
                && !this.data.isSpectating()
                && data.elapsed(data.getLastInLiquid()) > 25
                && !data.isInUnloadedChunk()
                && data.elapsed(data.getPlaceTicks()) > Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L) + 3)
                && data.elapsed(data.getLastRiptide()) > 30
                && data.elapsed(data.getLastGlide()) > 30
                && !data.isPossiblyTeleporting()
                && (data.isHasReceivedTransaction() || data.getTotalTicks() > 100)
                && this.data.getGameMode() != GameMode.CREATIVE
                && !data.isOnGhostBlock()
                && !data.isInBed()
                && !data.isLastInBed()
                && !data.isRiding()
                && this.data.elapsed(data.getLastInPowder()) > 3
                && ((this.data.getAirTicks() > 30 + jumpTicks)
                || this.data.getClientAirTicks() > 30 + jumpTicks)
                && this.data.elapsed(data.getLastFlyTick()) > 80;

        double addition = (MathUtil.isNearlySame(data.deltas.motionY, 0.33, 0.01)
                || MathUtil.isNearlySame(data.deltas.motionY, 0.24, 0.01)
                || MathUtil.isNearlySame(data.deltas.motionY, 0.16, 0.01)
                || MathUtil.isNearlySame(data.deltas.motionY, 0.08, 0.01)
                || MathUtil.isNearlySame(data.deltas.motionY, 0.00, 0.01))
                && data.elapsed(data.getPlaceTicks()) <= Math.min(20, MathUtil.getPingInTicks(data.getTransactionPing() + 50L) + 7) ? 0.05 : 1;

        addition = (addition == 0.05 && MathUtil.isNearlySame(data.deltas.motionY, data.deltas.lastMotionY, 0.021)) ? 0.5 : addition;

        if (this.data.deltas.motionY >= limit && valid && data.elapsed(data.getLastFlyTick()) > 30) {
            if ((this.violations += addition) > maxVL) {
                fail("* Accelerating upwards before being on ground" +
                        " \n §f* D: §b" + this.data.deltas.motionY
                        + "\n §f* ST/CT: §b" + this.data.getAirTicks() + " | " + this.data.getClientAirTicks(),
                        getBanVL(), 60
                );
            }
        } else {
            this.violations = Math.max(violations - 0.375, 0);
        }

    }


}


