package me.liwk.karhu.check.impl.combat.velocity;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import org.bukkit.util.Vector;

@CheckInfo(name = "Velocity (A)", category = Category.COMBAT, subCategory = SubCategory.VELOCITY, experimental = false)
public final class VelocityA extends PacketCheck {

    private double kbY, startKbY;

    private int posDesyncStreak, tick;

    public VelocityA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        if (packet instanceof FlyingEvent) {

            Vector tickVel = data.getTickedVelocity();

            double allowance = 0.0005;

            if (data.getTickedVelocity() != null) {
                this.kbY = tickVel.getY();
                this.startKbY = this.kbY;

                allowance += 0.00125;
            }


            if (data.elapsed(data.getLastRelativeVelo()) <= 20) {
                allowance += 0.14; //Don't question me
            }

            this.kbY = Math.abs(this.kbY) < (this.data.isNewerThan8() ? 0.003D : 0.005D) ? 0 : this.kbY;

            double dClientKb = this.data.deltas.motionY;

            if (this.kbY > 0) {

                if (this.data.elapsed(data.getLastOnClimbable()) > 3
                        && this.data.elapsed(data.getLastInWeb()) > 4
                        && !this.data.isPossiblyTeleporting()
                        && !this.data.isGliding()
                        && !this.data.isJumped()
                        && !this.data.isInsideBlock()
                        && data.isInitialized()
                        && this.data.elapsed(data.getLastFlyTick()) > 30
                        && this.data.elapsed(data.getLastInLiquid()) > 3
                        && !this.data.isRiding()
                        && this.data.elapsed(data.getLastInPowder()) > 3
                        && data.elapsed(data.getLastPistonPush()) > 3
                        && data.elapsed(data.getLastInBerry()) > 3
                        && this.data.elapsed(this.data.getLastOnBoat()) > 1
                        && this.data.elapsed(this.data.getLastOnSlime()) > 10
                        && this.data.elapsed(this.data.getLastInLiquid()) > 10) {

                    double tempKbY = this.kbY;

                    tempKbY -= 0.08D;
                    tempKbY *= 0.98F;

                    if (Math.abs(dClientKb - tempKbY) < 0.0001F) {
                        if (posDesyncStreak < 10) {
                            this.kbY = tempKbY;
                        }
                        ++posDesyncStreak;
                    } else {
                        --posDesyncStreak;
                    }

                    if (checkHori() && this.kbY < 0.09D && this.startKbY < 0.4D) {
                        this.resetState(dClientKb, 2);
                        return;
                    }

                    if (this.kbY < 0.0325) {
                        /*
                        Client might not move with this if their horizontal motion is also low
                        */
                        this.resetState(dClientKb, 3);
                        return;
                    }

                    //(Math.abs(dClientKb + (0.08D * 0.98F)) <= 0.03125)

                    if ((data.elapsed(this.data.getLastCollidedV()) < 1
                            || data.elapsed(this.data.getLastCollidedVGhost()) < 1) && data.deltas.motionY >= 0) {

                        this.resetState(dClientKb, 4);
                        return;
                    }

                    if (data.elapsed(data.getLastCollidedH()) <= 1
                            || data.elapsed(data.getLastCollidedGhost()) <= 1
                            || data.isCollidedWithFence()) {
                        /*
                        Step up half block while taking kb
                        */

                        if (Math.abs(0.5D - dClientKb) < 0.005) {
                            this.resetState(dClientKb, 5);
                            return;
                        }

                        if (Math.abs(0.375D - dClientKb) < 0.005) {
                            this.resetState(dClientKb, 5);
                            return;
                        }

                        if (Math.abs(0.5625D - dClientKb) < 0.005) {
                            this.resetState(dClientKb, 5);
                            return;
                        }
                    } else if(data.elapsed(data.getLastOnBoat()) <= 1) {
                        /*
                        Step up boat while taking kb
                        */
                        if (Math.abs(0.6D - dClientKb) < 0.005) {
                            this.resetState(dClientKb, 6);
                            return;
                        }
                    }


                    final double dKb = this.kbY;
                    final double ptc = (dClientKb / dKb) * 100.0D;

                    final double diff = Math.abs(dClientKb - dKb);

                    if (checkHori() && data.deltas.deltaXZ <= 0.1D) {
                        if (Math.abs(dKb - dClientKb) <= 0.03125) {
                            allowance = 0.05;
                        }
                    }

                    ++tick;

                    final double minPtc = this.data.getBukkitPlayer().getMaximumNoDamageTicks() < 5 ? 90.0D : 99.9915D;
                    final double maxPtc = this.data.getBukkitPlayer().getMaximumNoDamageTicks() < 10 ? 600.0D : 101.0D;

                    final double addition = 1 + Math.min(2, Math.abs(((dKb - dClientKb) * 1.5)));

                    if ((ptc < minPtc || ptc > maxPtc) && diff >= allowance) {
                        if ((this.violations += addition) > 3.5D) {
                            this.fail("* Vertical Modification"
                                            + "\n §f* approx pct: §b" + this.format(2, ptc)
                                            + "\n §f* client: §b" + dClientKb
                                            + "\n §f* server: §b" + dKb
                                            + "\n §f* coll: §b" + data.elapsed(data.getLastCollidedH())
                                            + "\n §f* tick: §b" + tick,
                                    getBanVL(), 300);
                        }
                        if (this.violations > 2) {
                            debug(String.format("PTC: %.3f cKB: %.3f", ptc, dClientKb));
                        }
                        this.resetState(dClientKb, 69);
                    } else {
                        this.violations = Math.max(this.violations - 0.125D, 0.0D);
                    }

                    this.kbY -= 0.08D;
                    this.kbY *= 0.98F;

                    if (this.data.isOnGroundPacket() || kbY == 0 || tick > 10) {
                        this.resetState(dClientKb, 420);
                    }

                } else {
                    this.resetState(dClientKb, 1);
                }

            }
        }

    }

    public boolean checkHori() {
        return data.isCollidedHorizontally() || data.elapsed(data.getLastCollidedGhost()) <= 1;
    }

    private void resetState(double dClientKb, int state) {
        kbY = 0;
        tick = 0;
        //debugMisc(String.format("RESET cKB: %.3f STATE: %d", dClientKb, state));
    }

}
