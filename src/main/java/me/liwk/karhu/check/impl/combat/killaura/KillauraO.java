package me.liwk.karhu.check.impl.combat.killaura;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.TickEndEvent;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;

@CheckInfo(name = "Killaura (O)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = true)
public final class KillauraO extends PacketCheck {

    private int targetChanges, lastEntityId, attacksInWindow, distanceSuspicious, ticksSinceLastSwitch;
    private double totalYawChange;
    private static final int MAX_CHANGES_PER_SECOND = 4;
    private static final int SAMPLE_TICKS = 20;
    private static final float SUSPICIOUS_YAW_THRESHOLD = 10F;

    public KillauraO(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
        this.lastEntityId = -1;
        this.totalYawChange = 0;
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof AttackEvent) {
            attacksInWindow++;
            int currentTarget = ((AttackEvent) packet).getEntityId();

            // Check if target changed
            if (currentTarget != lastEntityId && lastEntityId != -1) {

                EntityData edata = data.getEntityData().get(currentTarget);
                EntityData edata2 = data.getEntityData().get(lastEntityId);

                if (edata != null && edata2 != null) {

                    AxisAlignedBB entityBBCurr = edata.getEntityBoundingBox();
                    AxisAlignedBB entityBBLast = edata2.getEntityBoundingBox();

                    double entityDistance = entityBBCurr.distance(entityBBLast);

                    if (entityDistance > 0.7) {
                        targetChanges++;
                    }

                    double distToNew = data.getBoundingBox().distanceToHitbox(entityBBCurr);
                    double distToOld = data.getBoundingBox().distanceToHitbox(entityBBLast);

                    if (distToNew < distToOld) {
                        ++distanceSuspicious;
                    }

                    // Targets are far apart = very suspicious
                    if (entityDistance > 3.0) {
                        ++distanceSuspicious;
                    }

                    // Rapid switch between distant targets = extremely suspicious
                    if (entityDistance > 3.0 && ticksSinceLastSwitch < 5) {
                        if (increase(1) > 2) {
                            fail(String.format("* Combat analysis 2"
                                            + "\n §f* std: §b (§fd=§b%.2f§f, t=§b%d°)",
                                    entityDistance, ticksSinceLastSwitch), 200L);
                        }
                    } else {
                        decrease(0.2);
                    }
                }
                ticksSinceLastSwitch = 0;
            }

            lastEntityId = currentTarget;
        } else if (packet instanceof FlyingEvent) {
            tick();
        } else if (packet instanceof TickEndEvent && data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
            tick();
        }

    }

    public void tick() {
        ticksSinceLastSwitch++;

        // Check for camera snap 1 tick after attack
        if (data.getLastAttackTick() <= 1) {
            float deltaYaw = Math.max(data.deltas.deltaYaw, data.deltas.lDeltaYaw);

            // Suspicious yaw change right after attack
            if (deltaYaw > SUSPICIOUS_YAW_THRESHOLD) {
                totalYawChange += deltaYaw;
            }
        }

        // Check for excessive switching over sample window
        if (data.getTotalTicks() % SAMPLE_TICKS == 0) {
            if (targetChanges > 0) {
                // Combine target changes with camera movement analysis
                boolean excessiveSwitching = targetChanges >= MAX_CHANGES_PER_SECOND;
                boolean suspiciousCamera = totalYawChange >= 40F; // High accumulated yaw change
                float switchRate = targetChanges / (float) Math.max(1, attacksInWindow);
                float distanceRate = (float) distanceSuspicious / targetChanges;
                boolean highSwitchRate = switchRate > 0.6F;
                boolean distanceSwitchRate = distanceRate > 0.6F;

                if (excessiveSwitching && suspiciousCamera && (highSwitchRate || distanceSwitchRate)) {
                    fail(String.format("* Combat analysis"
                                    + "\n §f* (§fc=§b%d/t§f, d=§b%.2f°)",
                            targetChanges, totalYawChange), 200L);
                }
            }

            // Reset counters
            targetChanges = 0;
            totalYawChange = 0;
            distanceSuspicious = 0;
            attacksInWindow = 0;
        }
    }
}
