package me.liwk.karhu.check.impl.combat.velocity;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.util.Vector;

@CheckInfo(name = "Velocity (B)", category = Category.COMBAT, subCategory = SubCategory.VELOCITY, experimental = false)
public final class  VelocityB extends PacketCheck {

    private double allowance;

    private int attacks;

    private boolean canCheck;

    public VelocityB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        if (packet instanceof FlyingEvent) {

            Vector tickVel = data.getTickedVelocity();

            if (tickVel != null) {
                allowance = 1.25E-3;
                canCheck = true;
            } else {
                allowance = 0.0005;
            }

            if (data.elapsed(data.getLastRelativeVelo()) <= 8) {
                allowance += ((data.offsetMove() + data.clamp()) * 2.5);
            }

            if (data.getMoveTicks() <= 3 || data.elapsed(data.getPredictionTicks()) <= 3) {
                allowance = data.offsetMove() + data.clamp();
            } else if (data.deltas.deltaXZ <= data.offsetMove()) {
                allowance = data.offsetMove() + data.clamp();
            }

            boolean entity = data.elapsed(data.getLastCollidedWithEntity()) <= 3;

            if (entity) {
                allowance += 0.1;
            }

            if (data.isLegacy()) {
                //moveFlying code is slightly different, account for it
                allowance += 0.0005;
            }

            if (canCheckCondition() && canCheck) {

                if (data.elapsed(data.getLastInWeb()) > 4 &&
                        !data.isGliding() &&
                        !data.isRiding() &&
                        data.elapsed(data.getLastInBerry()) > 2 &&
                        data.elapsed(data.getLastSneakEdge()) > 5 &&
                        !data.isPossiblyTeleporting() &&
                        data.elapsed(data.getLastOnClimbable()) > 5 &&
                        data.elapsed(data.getLastInLiquid()) > 5 &&
                        this.data.elapsed(data.getLastInPowder()) > 3 &&
                        data.elapsed(data.getLastOnBoat()) > 1 &&
                        data.elapsed(data.getLastCollided()) > 1 &&
                        data.elapsed(data.getLastCollidedGhost()) > 1) {


                    double dKbZ = data.deltas.deltaX / simulation.getOutputX();
                    double dKbX = data.deltas.deltaZ / simulation.getOutputZ();

                    final double percent = (data.deltas.deltaXZ / simulation.getOutputXZ()) * 100.0D;

                    final boolean reversed = (dKbZ < -0.05 || dKbX < -0.05);

                    if ((percent < 99.99 || reversed) && simulation.getLowestMatch() > allowance) {
                        if (increase(1) > 3) {
                            this.fail("* Horizontal Modification"
                                    + "\n §f* approx pct: §b" + this.format(4, percent)
                                    + "\n §f* client: §b" + this.format(4, data.deltas.deltaXZ) + " | " + simulation.getLowestMatch()
                                    + "\n §f* server: §b" + this.format(4, simulation.getOutputXZ())
                                    + "\n §f* jump: §b" + this.data.isJumped()
                                    + "\n §f* tick: §b" + data.elapsed(data.getLastVelocityTaken()) + " | " + this.data.getMoveTicks()
                                    + "\n §f* attack: §b" + simulation.isAttacking() + " | " + this.data.getLastAttackTick() + " | " + attacks
                                    + "\n §f* version: §b" + MathUtil.parseVersion(this.data.getClientVersion())
                                    + "\n §f* reverse: §b" + reversed + " | " + this.format(3, dKbX) + " | " + this.format(3, dKbZ), getBanVL(), 300);
                        }
                        resetState();
                    } else {
                        decrease(0.075D);
                    }
                }

                if (data.elapsed(data.getLastVelocityTaken()) > 8) {
                    resetState();
                }
            } else {
                resetState();
            }

            attacks = 0;

        } else if (packet instanceof AttackEvent) {
            if(((AttackEvent) packet).isPlayer()) {
                /*if(data.getStackInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0) {
                    attack = false;
                } else {*/
                ++attacks;
            }
        }
    }

    private void resetState() {
        canCheck = false;
    }

    private boolean canCheckCondition() {
        return data.elapsed(data.getLastVelocityTaken()) <= 8
                && data.elapsed(data.getLastFlyTick()) > 30;
    }
}