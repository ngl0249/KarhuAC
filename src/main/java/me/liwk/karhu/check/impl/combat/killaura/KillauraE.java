package me.liwk.karhu.check.impl.combat.killaura;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;

@CheckInfo(name = "Killaura (E)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false)
public final class KillauraE extends PacketCheck {

    private int attacks;

    public KillauraE(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        //OUTDATED

        /*if (packet instanceof FlyingEvent) {

            if(attacks > 0) {

                if (!data.isRiding()
                        && data.deltas.deltaXZ > 0.1
                        && data.elapsed(data.getLastOnIce()) > 2
                        && data.getVelocityXZTicks() > 1
                        && data.getLastTarget() instanceof Player
                        && !data.isSpectating()
                        && data.elapsed(data.getLastFlyTick()) > 30) {

                    double deltaX = data.deltas.lastDX *= data.isLastLastOnGroundPacket()
                            ? data.getLastTickFriction()
                            : 0.91F;

                    double deltaZ = data.deltas.lastDZ *= data.isLastLastOnGroundPacket()
                            ? data.getLastTickFriction()
                            : 0.91F;

                    deltaX *= 0.6D;
                    deltaZ *= 0.6D;

                    final double deltaXZ = MathUtil.hypot(deltaX, deltaZ);
                    final double attackMotion = Math.abs(data.deltas.deltaXZ - deltaXZ);
                    final double acceleration = Math.abs(data.deltas.accelXZ);

                    final double addition = data.isSprinting() ? 1.1 : 0.5;
                    double moveSpeed = data.getWalkSpeed();

                    moveSpeed += data.getWalkSpeed() * 0.3F;

                    if (attackMotion > moveSpeed && acceleration < 0.005D) {

                        if ((violations += addition) > 6) {
                            fail("* Invalid motion when attacking" +
                                    "\n §f* motion: §b" + attackMotion + "/" + moveSpeed +
                                    "\n §f* acceleration: §b" + acceleration +
                                    "\n §f* attacks: §b" + attacks, getBanVL(), 600L);
                        } else {
                            debug("* Invalid motion when attacking" +
                                    "\n §f* motion: §b" + attackMotion + "/" + moveSpeed +
                                    "\n §f* acceleration: §b" + acceleration +
                                    "\n §f* attacks: §b" + attacks);
                        }
                    } else {
                        decrease(0.4);
                    }

                } else {
                    decrease(0.2);
                }
            }

            attacks = 0;

        } else if(packet instanceof AttackEvent) {
            if(data.getStackInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) < 0 || data.getLastTarget() == null) return;

            if(data.isWasSprinting() || data.getStackInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0) {
                ++attacks;
            }
        }*/
    }
}
