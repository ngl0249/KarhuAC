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
import me.liwk.karhu.util.player.PlayerUtil;

@CheckInfo(name = "Fly (E)", category = Category.MOVEMENT, subCategory = SubCategory.FLY, experimental = true)
public final class FlyE extends PacketCheck {

    private int zeroPointThree;

    public FlyE(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event p) {

        if (p instanceof FlyingEvent && ((FlyingEvent) p).hasMoved()) {
            if (this.data.elapsed(this.data.getLastRiptide()) > 40 &&
                    !this.data.isPossiblyTeleporting() &&
                    !this.data.isInUnloadedChunk() &&
                    !this.data.isSpectating() &&
                    !this.data.isOnPiston() &&
                    !this.data.isOnFence() &&
                    !this.data.isOnLava() &&
                    !this.data.isWasOnFence() &&
                    !data.rodPullAffecting() &&
                    data.elapsed(data.getLastPistonPush()) > 2 &&
                    this.data.getLevitationLevel() == 0 &&
                    this.data.getSlowFallingLevel() == 0 &&
                    this.data.elapsed(this.data.getLastOnBed()) > 5 &&
                    data.elapsed(data.getLastInBerry()) > 1 &&
                    this.data.elapsed(data.getLastInPowder()) > 7 &&
                    data.elapsed(data.getLastGlide()) > 140 &&
                    data.elapsed(data.getLastOnHoneySide()) > 3 &&
                    !this.data.isOnScaffolding() &&
                    this.data.getGameMode() != GameMode.CREATIVE &&
                    (!this.data.isInWeb() && !this.data.isWasInWeb() && !this.data.isWasWasInWeb())) {

                boolean executePreds = true;

                double extraThreshold = 0.0;

                if((((this.data.elapsed(data.getLastCollidedV()) <= 3 && data.getClientAirTicks() <= 20)
                        || this.data.isUnderGhostBlock()) && data.getClientAirTicks() <= 10)
                        || (this.data.isInsideBlock() && data.getClientAirTicks() <= 20)) {
                    if(!data.isOnLadder()) extraThreshold = 0.4;
                }

                double clamp = this.data.isNewerThan8() ? 0.003D : 0.005D;

                final double motionY = data.deltas.motionY;
                final double lastMotionY = Math.abs(data.deltas.lastMotionY) < clamp ? 0 : data.deltas.lastMotionY;
                final double lastLastMotionY = data.deltas.lastLastMotionY;

                final double jumpHeight = PlayerUtil.getJumpHeight(data);
                final double maxiumViolations = data.isOnLadder() || data.isLastLadder() ? 8 : 4.5;

                boolean ignoreServerGround = false;
                boolean trappa = false;

                double pred = data.getVelocityYTicks() == 0 ? data.getVelocityY() : (lastMotionY - 0.08) * 0.98F;
                double threshold = this.data.elapsed(this.data.getLastOnSlime()) <= 30 ? 0.2D : 0.005;

                if (data.isOnLadder() || data.isLastLadder()) { //MCP Reversed method
                    //CustomLocation location = data.getLocation();

                    boolean goForward = true;

                    if (data.isJumped()) {
                        pred = PlayerUtil.getJumpHeight(data);
                        goForward = false;
                    } else if (data.getVelocityYTicks() == 0) {
                        pred = data.getVelocityY();
                        goForward = false;
                    }

                    if (goForward) {
                        if (data.isLastLadder() && motionY < 0.03125) {

                            pred = Math.max(pred, -0.15D * 0.98F);

                            if (data.isSneaking() && motionY <= 0.03) {
                                pred = 0.0D;
                            }
                        }

                        if (motionY > 0.07 && data.getClientAirTicks() > 3) {
                            pred = (0.2D - 0.08D) * 0.98F;
                            trappa = true;
                        }
                    }

                    ignoreServerGround = true;

                    if (data.getClientAirTicks() == 3) threshold += 1; //Nice value patch gg

                    threshold += 0.085;
                }

                if (data.getVelocityYTicks() <= 3) {
                    threshold += 0.4D;
                }

                if (data.elapsed(data.getLastRelativeVelo()) <= 20) {
                    threshold += 1D;
                }

                if (data.isWasOnWater()) {
                    double fixedLastMotion = lastMotionY;
                    if (motionY > 0) {
                        fixedLastMotion += 0.04F;
                    }
                    pred = data.getVelocityYTicks() == 0 ? data.getVelocityY() : (fixedLastMotion * 0.800000011920929D) - 0.02D;
                    threshold += 0.6D;
                } else if (data.isOnWater()) {
                    threshold += 0.6D;
                    threshold += data.elapsed(data.getLastCollided()) <= 1 ? 0.1D : 0.0D;
                }

                int lastInLiquid = data.elapsed(data.getLastInLiquid());
                if (!data.isWasOnWater() && lastInLiquid > 2 && lastInLiquid <= 5 + Math.min(15, data.getPingInTicks())) {
                    threshold += 0.1;
                }

                if (data.isWasOnLava()) {
                    double fixedLastMotion = lastMotionY;
                    if (motionY > 0) {
                        fixedLastMotion += 0.04F;
                    }
                    pred = data.getVelocityYTicks() == 0 ? data.getVelocityY() : (fixedLastMotion * 0.5D) - 0.02D;
                    threshold += 0.6D;
                } else if (data.isOnLava()) {
                    threshold += 0.6D;
                    threshold += data.elapsed(data.getLastCollided()) <= 1 ? 0.1D : 0.0D;
                }

                if (lastInLiquid <= 10) { //Lunar client, nice job
                    if (lastInLiquid <= 2) {
                        threshold += 0.12D;
                    } else {
                        threshold += 0.06D;
                    }
                }

                if (data.elapsed(data.getPredictionTicks()) <= 1 || data.getMoveTicks() <= 1) {
                    if (zeroPointThree <= 10) {
                        threshold += 0.0425;
                    }
                    ++zeroPointThree;
                } else {
                    --zeroPointThree;
                }

                if (!data.isTakingVertical() && data.elapsed(data.getLastVelocityTaken()) <= 10 && data.isCollidedHorizontally()) {
                    threshold += 0.05;
                }

                if (Math.abs(motionY + 0.098) <= 1E-5) return;
                if (data.getLocation().y <= 0 && motionY == 0) return;


                if (data.elapsed(data.getPlaceTicks()) < Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L) + 20)) {
                    threshold += 0.05;
                }

                if (data.getJumpBoost() != 0 && data.getMoveTicks() <= 2) threshold += 0.1;
                if (data.isTakingVertical()) threshold += 0.1;
                if (data.isNearClimbable()) threshold += 0.35F;

                final double clampedPred = Math.abs(pred) < clamp ? -0.08 * 0.98F : pred;
                final double ratio = Math.abs(motionY - clampedPred);

                final double ratioAndIdcEtc = trappa ? 0 : Math.min(3, Math.ceil(ratio * 2.5));

                if (ratio >= threshold + extraThreshold && Math.abs(pred) > 0.03 + clamp
                        && data.elapsed(data.getLastOnSlime()) > 1
                        && !data.isOnBoat()
                        && (data.getClientAirTicks() > 2 && (data.getAirTicks() > 2 || ignoreServerGround))) {
                    if ((violations += 1 + ratioAndIdcEtc) > maxiumViolations) {

                        if (data.elapsed(data.getLastFlyTick()) <= 6) {
                            if (data.isConfirmingFlying() && !data.getBukkitPlayer().getAllowFlight() && data.elapsed(data.getLastConfirmingState()) > 3) {
                                //pullback(data.getFlyCancel());
                            }
                        } else {
                            fail("* Generic gravity modification" +
                                            " \n §f* PRED §b" + pred +
                                            " \n §f* MOTION §b" + motionY + "/" + format(3, data.deltas.deltaXZ) +
                                            " \n §f* RAT §b" + ratio +
                                            " \n §f* TR §b" + threshold +
                                            " \n §f* IGS §b" + ignoreServerGround +
                                            " \n §f* F: §b" + data.elapsed(data.getLastFlyTick()) + " | " + lastInLiquid +
                                            " \n §f* ST/CT: §b" + this.data.getAirTicks() + " | " + this.data.getClientAirTicks(),
                                    getBanVL(), 325);
                        }
                    }
                } else {
                    violations = Math.max(violations - 0.04, 0);
                }

                if (data.isWasSlimeLand() && data.elapsed(data.getLastPistonPush()) >= 5 && !data.isOnPiston()) {
                    double maxSlime = Math.max(Math.abs(lastMotionY), Math.abs(lastLastMotionY) + 0.2D);
                    if (motionY >= (maxSlime + Math.abs(data.getVelocityY()) + extraThreshold) && motionY > jumpHeight + 0.2F) {
                        fail("* Generic gravity modification (slime)" +
                                        " \n §f* PRED §b" + pred +
                                        " \n §f* MOTION §b" + motionY + "/" + maxSlime +
                                        " \n §f* TR §b" + threshold +
                                        " \n §f* ST/CT: §b" + this.data.getAirTicks() + " | " + this.data.getClientAirTicks(),
                                getBanVL(), 325);
                    }
                }

            } else {
                violations = Math.max(violations - 0.0075, 0);
            }
        }
    }
}