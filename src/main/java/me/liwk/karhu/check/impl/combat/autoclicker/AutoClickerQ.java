package me.liwk.karhu.check.impl.combat.autoclicker;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;
import me.liwk.karhu.util.MathUtil;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckInfo(name = "AutoClicker (Q)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = true)
public final class AutoClickerQ extends PacketCheck {

    public AutoClickerQ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private int attacks, swings, swingsNearEntity, lastAttack;

    private boolean nearEntity;

    private double lastX, lastZ;

    private final Deque<Integer> delays = new ArrayDeque<>();

    @Override
    public void handle(final Event packet) {

        if (packet instanceof AttackEvent) {
            ++attacks;

            int delay = data.getTotalTicks() - lastAttack;

            if (delay >= 5 && delay <= 25 && nearEntity) {

                delays.add(delay);

                if (delays.size() == 50) {
                    double std = MathUtil.getStandardDeviation(delays);

                    Check<?> autoClickerR = data.getCheckManager().getCheck(AutoClickerR.class);

                    if (Karhu.getInstance().getCheckState().isEnabled(autoClickerR.getName())) {
                        if (std <= 1.5) {
                            autoClickerR.fail("* Attack analysis"
                                    + "\n§f* R: §b" + std, autoClickerR.getBanVL(), 300L);
                        }
                        autoClickerR.debug("* Attack analysis"
                                + "\n§f* R: §b" + std);
                    }

                    delays.clear();
                }
            }

            lastAttack = data.getTotalTicks();
        } else if(packet instanceof SwingEvent) {
            ++swings;

            if (nearEntity) {
                ++swingsNearEntity;
            }

            if (swingsNearEntity == 50) {
                double ratioA = (double) attacks / swings;
                double ratioS = (double) swingsNearEntity / swings;

                if (ratioA >= 0.75 && ratioS > 0.9) {
                    fail("* Attack analysis"
                                    + "\n§f* R: §b" + format(3, ratioA) + "/" + format(3, ratioS)
                                    + "\n§f* a/s/es: §b" + attacks + "/" + swings + "/" + swingsNearEntity,
                            getBanVL(), 300L);
                }

                debug("* Attack analysis"
                        + "\n§f* R: §b" + format(3, ratioA) + "/" + format(3, ratioS)
                        + "\n§f* a/s/es: §b" + attacks + "/" + swings + "/" + swingsNearEntity);

                attacks = swingsNearEntity = swings = 0;

                nearEntity = false;
            }

        } else if (packet instanceof FlyingEvent) {
            checkEntity();
        } else if (packet instanceof TickEndEvent && data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
            checkEntity();
        }
    }

    public void checkEntity() {
        if (data.getLastAttackTick() <= 1 && data.getLastTarget() != -696969) {
            EntityData edata = data.getEntityData().get(data.getLastTarget());

            if (edata == null) return;

            double x = edata.getEntityBoundingBox().getCenterX(), z = edata.getEntityBoundingBox().getCenterZ();

            final double distance = data.getBoundingBox().distance(x, z);

            final boolean movement = data.deltas.deltaXZ >= 0.08D
                    && (data.deltas.deltaYaw >= 0.0 || data.deltas.lDeltaYaw >= 0.0)
                    && data.elapsed(data.getLastVelocityTaken()) <= 150
                    && (Math.abs(x - lastX) >= 0.0325 && Math.abs(z - lastZ) >= 0.0325);

            if (distance <= 3 && movement) {
                nearEntity = true;
            } else {
                nearEntity = false;
            }

            this.lastX = x;
            this.lastZ = z;
        }
    }
}
