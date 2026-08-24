package me.liwk.karhu.check.impl.movement.fly;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;

/*
 *    @Fly A
 *    Checks for improper y-axis moves
 */

@CheckInfo(name = "Fly (A)", category = Category.MOVEMENT, subCategory = SubCategory.FLY, experimental = false)
public final class FlyA extends PacketCheck {

    public double fucked;
    public static double GRAVITY = 0.08D * 0.9800000190734863D;

    public FlyA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {

        if (packet instanceof FlyingEvent && ((FlyingEvent) packet).hasMoved()) {

            if (this.data.elapsed(this.data.getLastRiptide()) >= 5 &&
                    !this.data.isInUnloadedChunk() &&
                    !this.data.isSpectating() &&
                    !this.data.isOnPiston() &&
                    !this.data.rodPullAffecting() &&
                    this.data.elapsed(data.getLastInLiquid()) > 3 &&
                    this.data.getLevitationLevel() == 0 &&
                    this.data.getSlowFallingLevel() == 0 &&
                    this.data.elapsed(data.getLastFlyTick()) > 30 &&
                    this.data.elapsed(this.data.getLastOnBed()) > 5 &&
                    this.data.elapsed(data.getLastInPowder()) > 6 &&
                    !this.data.isPossiblyTeleporting() &&
                    this.data.elapsed(data.getLastGlide()) > 140 &&
                    !this.data.isOnScaffolding() &&
                    this.data.getGameMode() != GameMode.CREATIVE &&
                    (!this.data.isInWeb() && !this.data.isWasInWeb() && !this.data.isWasWasInWeb())) {


                if ((this.data.elapsed(data.getLastCollidedV()) <= 4
                        || (this.data.isInsideBlock() && data.getClientAirTicks() <= 20))) {
                    return;
                }

                if (data.isOnLadder() || data.isLastLadder()) { //MCP Reversed method
                    return;
                }

                double lastLastDeltaY = data.deltas.lastLastMotionY;
                double lastDeltaY = data.deltas.lastMotionY;
                double deltaY = data.deltas.motionY;

                double chunkMove = data.getLocation().y > 0.0D ? 0.09800000190735147D : 0;

                boolean unload = Math.abs(data.deltas.motionY + chunkMove) <= 1E-7;

                if (unload) { return; } // PAK PAK PAK PAKKO HANDLAA THIS ELSEWHERE HAHAHAH

                final boolean velocity = data.getVelocityYTicks() == 0;

                double clamp = data.clamp();

                if (data.getMoveTicks() == 1) {
                    lastDeltaY = (lastLastDeltaY - 0.08D) * 0.9800000190734863D;

                    if (velocity) lastDeltaY = data.getVelocityY();
                    if (Math.abs(lastDeltaY) < clamp) lastDeltaY = 0.0D;
                }

                double motionY = (lastDeltaY - 0.08D) * 0.9800000190734863D;

                if (velocity) motionY = data.getVelocityY();
                if (Math.abs(motionY) < clamp) motionY = 0;
                if (data.getTeleportManager().teleportTicks <= 1) motionY = 0.0D;

                final double difference = Math.abs(deltaY - motionY);

                double lenience = getLenience();

                final boolean invalid = difference > lenience
                        //&& Math.abs(motionY) > 0.0025 + clamp
                        && !data.isOnGroundPacket()
                        && !data.isLastOnGroundPacket();

                if (invalid) {
                    if (++fucked > 4) {
                        this.fail(String.format("* Invalid y-axis movement:"
                                        + "\ndiff: %s len: %s"
                                        + "\nmotionY: %s deltaY: %s"
                                        + "\ntp: %s move: %s collideV: %s",
                                difference, lenience,
                                motionY, deltaY,
                                data.getTeleportManager().teleportTicks, data.getClientAirTicks(),
                                data.elapsed(data.getLastCollidedV())), 300L);
                    }
                } else {
                    fucked = Math.max(0, fucked - 0.05);
                }
            } else {
                fucked = Math.max(0, fucked - 0.005);
            }
        }
    }

    private double getLenience() {
        /*Okay so, what if 0.03 happens for longer than 1 tick maybe on teleport?
        It's now adjusted to allow 1 extra movement if 0.03, could've happened.
        This shouldn't create too big of a bypass
         */
        double lenience = (data.getMoveTicks() <= 3 || data.elapsed(data.getPredictionTicks()) <= 1) ? GRAVITY : 1E-05D;

        int pingTicks = MathUtil.getPingInTicks(data.getTransactionPing() + 50L);

        if (data.elapsed(data.getPlaceTicks()) < Math.min(15, pingTicks + 10)) lenience += 0.05;

        //Idk really what is up with this :/
        // Player can jump same tick of teleport
        if (data.getTeleportManager().teleportTicks <= 1) lenience += 0.42F;
        //lenience += (GRAVITY + data.clamp());

        if (!data.isTakingVertical()
                && data.elapsed(data.getLastVelocityTaken()) <= 10
                && data.isCollidedHorizontally()) lenience += 0.05;

        if (data.isTakingVertical()) lenience += 0.1;
        if (data.isOnSlime() || data.isWasOnSlime()) lenience += data.offsetMove();

        //AIDS
        if (data.elapsed(data.getLastPistonPush()) <= 5) lenience += 0.5;

        if (data.isNewerThan12() && data.elapsed(data.getLastInLiquid()) <= 8 || data.isOnWater()) lenience += 0.0022;

        if (data.elapsed(data.getLastInLiquid()) <= 5 + Math.min(15, pingTicks)) {
            lenience += 0.1F;
        }

        //Where is this value from u might ask, it's from my brain...
        if (this.data.elapsed(data.getLastCollidedVGhost()) <= 3) lenience += 0.3;

        if (data.isNearClimbable()) lenience += 0.35F;

        if (data.isOnHoney() || data.isWasOnHoney()) lenience += 0.2F;

        if (data.elapsed(data.getLastOnHoneySide()) <= 3) lenience += 0.2F;

        if (data.isOnFence() || data.isWasOnFence()) lenience += 0.1F;

        if (this.data.elapsed(data.getLastInBerry()) <= 4) lenience += 0.4F;

        return lenience;
    }
}