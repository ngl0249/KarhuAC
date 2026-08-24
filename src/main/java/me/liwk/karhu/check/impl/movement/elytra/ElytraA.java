package me.liwk.karhu.check.impl.movement.elytra;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Elytra (A)", category = Category.MOVEMENT, subCategory = SubCategory.FLY, experimental = true)
public final class ElytraA extends PositionCheck {

    private int upwardsTicks;
    private int accelerationTicks;
    private static final double MAX_ACCELERATION = 0.2;
    private static final double MIN_ELYTRA_SPEED = 0.05;
    private static final double MAX_ELYTRA_SPEED = 3.5;
    private static final double BASE_DRAG = 0.99;

    public ElytraA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(MovementUpdate e) {

        if (!data.isGliding()) {
            resetViolation();
            return;
        }

        if (data.getTickedVelocity() != null) {
            resetViolation();
            return;
        }

        float pitch = data.getLocation().getPitch();
        double deltaY = data.deltas.motionY;
        double lastDeltaY = data.deltas.lastMotionY;
        double deltaXZ = data.deltas.deltaXZ;
        double lastDeltaXZ = data.deltas.lastDXZ;

        boolean riptiding = data.elapsed(data.getLastRiptide()) <= 120;

        if (!riptiding) {
            if (pitch > 0 && deltaY > 0.01 && lastDeltaY > 0.01) {
                if (++upwardsTicks > 25) {
                    if (increase(0.4) > 1) {
                        fail("* Elytra invalid upwards " + data.deltas.motionY + " | " + pitch, 300L);
                    }
                    upwardsTicks = 5;
                    return;
                }
            } else {
                upwardsTicks = Math.max(0, upwardsTicks - 1);
            }

            if (data.deltas.motionY > 1.8) {
                if (increase(0.5) > 1) {
                    fail("* Elytra MAX UPWARDS MOVE " + data.deltas.motionY + " | " + pitch, 300L);
                }
                return;
            }
        }


        double expectedSpeed = lastDeltaXZ * BASE_DRAG;
        expectedSpeed -= (lastDeltaXZ * lastDeltaXZ * 0.0001);

        double pitchFactor = 1.0 + (Math.abs(pitch) / 90.0) * 0.2;
        expectedSpeed *= pitchFactor;

        double acceleration = deltaXZ - expectedSpeed;
        double expectedMaxAccel = MAX_ACCELERATION;

        if (data.deltas.lastDXZ < 1.0) {
            expectedMaxAccel *= 1.5;
        }

        if (pitch < -30) {
            expectedMaxAccel *= 1 + Math.abs(pitch / 90.0);
        }

        if (acceleration > expectedMaxAccel && deltaXZ > MIN_ELYTRA_SPEED) {
            if (++accelerationTicks > 3) {
                if (increase(0.5) > 1) {
                    fail("* Elytra acceleration " + String.format("a=%.3f exp=%.3f sp=%.2f drag=%.3f",
                            acceleration, expectedMaxAccel, deltaXZ, expectedSpeed), 300L);
                    return;
                }
                accelerationTicks = 0;
            }
        } else {
            accelerationTicks = Math.max(0, accelerationTicks - 1);
        }

        double expectedMaxSpeed = MAX_ELYTRA_SPEED;

        if (pitch < -30) {
            expectedMaxSpeed *= 1 + Math.abs(pitch / 45.0);
        }

        if (deltaXZ > expectedMaxSpeed && !riptiding) {
            if (increase(1) > 2) {
                fail("* Elytra maxspeed " + String.format("speed=%.2f max=%.2f", deltaXZ, expectedMaxSpeed), 300L);
                return;
            }
        }

        decrease(0.0025);
    }

    private void resetViolation() {
        upwardsTicks = 0;
        accelerationTicks = 0;
    }
}
