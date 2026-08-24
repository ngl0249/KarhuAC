package me.liwk.karhu.handler;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.vec.Vec2f;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.player.MovementUtils;
import me.liwk.karhu.world.nms.NMSValueParser;
import org.bukkit.util.Vector;

import static me.liwk.karhu.world.nms.NMSValueParser.BOOLEANS_REVERSED;

@RequiredArgsConstructor
public class SimulationHandler {

    private final KarhuPlayer data;

    @Getter
    private double outputX, outputZ, outputXZ, lowestMatch,
            testLowest, testOutputX, testOutputZ;

    @Getter
    private boolean sprinting, attacking, useItem, sneak, jumped;

    @Getter
    private float moveForward, moveStrafe, attributeSpeed, f5, blockFriction;

    @Getter
    private int scenarioAmount, edgeSneakTick;

    @Getter
    @Setter
    private float knownInputF, knownInputS;

    public void simulateMovement(double kbX, double kbZ, boolean test) {
        float friction = data.getCurrentFriction(),
                lastTickFriction = data.getLastTickFriction();

        Vector velocity = data.getTickedVelocity();

        if (!data.isNewerThan12()) friction *= 0.91F;

        boolean onGround = data.isLastOnGroundPacket();
        float yaw = data.getLocation().getYaw();

        // Reset all instance variables that will be used
        if (!test) {
            lowestMatch = Double.MAX_VALUE;
        } else {
            testLowest = Double.MAX_VALUE;
        }
        scenarioAmount = 0;

        boolean bruteforceSprint = data.isResettingSprint()
                || data.elapsed(data.getSprintAttributeTick()) <= 3
                || data.isMetadataSprint()
                || data.isSettingMetadataSprint();

        mainLoop:
        {
            for (float[] floats : NMSValueParser.KEY_COMBOS) {
                for (boolean attack : BOOLEANS_REVERSED) {
                    for (boolean using : BOOLEANS_REVERSED) {
                        //Too lazy to track sprint properly, if server changes it lets just bruteforce :)
                        if (bruteforceSprint) {
                            for (boolean sprint : BOOLEANS_REVERSED) {
                                if (processScenario(floats, attack, using, sprint, kbX, kbZ, test,
                                        friction, lastTickFriction, velocity, onGround, yaw)) {
                                    break mainLoop;
                                }
                            }
                        } else {
                            if (processScenario(floats, attack, using, data.isSprinting(), kbX, kbZ, test,
                                    friction, lastTickFriction, velocity, onGround, yaw)) {
                                break mainLoop;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean processScenario(float[] floats, boolean attack, boolean using, boolean sprint,
                                    double kbX, double kbZ, boolean test,
                                    float friction, float lastTickFriction,
                                    Vector velocity, boolean onGround, float yaw) {

        double moveForward = floats[1], moveStrafe = floats[0];

        for (boolean sneaking : BOOLEANS_REVERSED) {
            for (boolean jump : BOOLEANS_REVERSED) {
                if (attack && (data.getLastAttackTick() > 2 || data.getLastTarget() == -696969))
                    continue; //skip impossible case

                double currentMoveForward = moveForward;
                double currentMoveStrafe = moveStrafe;

                if (sneaking) {
                    float multiplier = 0.3f;
                    if (data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19)) {
                        multiplier = MathUtil.clampFloat(0.3F + (MovementUtils.getSwiftSneakevel(data.getBukkitPlayer()) * 0.15F), 0f, 1f);
                    }
                    currentMoveForward = currentMoveForward * multiplier;
                    currentMoveStrafe = currentMoveStrafe * multiplier;
                }

                float forward = (float) currentMoveForward;
                float strafe = (float) currentMoveStrafe;

                if (using) {
                    forward = forward * 0.2f;
                    strafe = strafe * 0.2f;
                }

                forward *= 0.98f;
                strafe *= 0.98f;

                double moveSpeed = data.getWalkSpeedDouble();
                double lastDX = data.deltas.lastDX, lastDZ = data.deltas.lastDZ;

                if (!data.isWasWasOnWater()) {
                    lastDX *= data.isLastLastOnGroundPacket() ? lastTickFriction * 0.91F : 0.91F;
                    lastDZ *= data.isLastLastOnGroundPacket() ? lastTickFriction * 0.91F : 0.91F;
                } else {
                    float f3 = (float) data.getDepthStriderLevel();
                    float f9 = sprint && data.isNewerThan12()
                            ? 0.9F
                            : 0.8F;

                    if (f3 > 3.0F) {
                        f3 = 3.0F;
                    }

                    if (!onGround) {
                        f3 *= 0.5F;
                    }

                    if (f3 > 0.0F) {
                        f9 += (0.54600006F - f9) * f3 / 3.0F;
                    }

                    if (data.getDolphinLevel() > 0) {
                        f9 = 0.96F;
                    }

                    lastDX *= f9;
                    lastDZ *= f9;
                }

                if (!test) {
                    if (velocity != null) {
                        lastDX = velocity.getX();
                        lastDZ = velocity.getZ();
                    }
                } else {
                    lastDX = kbX;
                    lastDZ = kbZ;
                }

                if (attack) {
                    lastDX *= 0.6;
                    lastDZ *= 0.6;
                }

                if (Math.abs(lastDX) < data.clamp()) lastDX = 0;
                if (Math.abs(lastDZ) < data.clamp()) lastDZ = 0;

                if (!data.isNewerThan12()) {
                    if (sprint) moveSpeed += moveSpeed * 0.3F;
                } else {
                    if (sprint) moveSpeed *= 1.0 + 0.3F;
                }


                float f5;

                if (!data.isWasOnWater()) {

                    if (onGround) {

                        if (!data.isNewerThan12()) {
                            f5 = ((float) moveSpeed) * (0.16277136f / (friction * friction * friction));
                        } else {
                            f5 = ((float) moveSpeed) * (0.21600002f / (friction * friction * friction));
                        }

                        if (jump && sprint) {
                            float radians = yaw * ((float) Math.PI / 180);
                            lastDX -= MathHelper.sin(radians) * 0.2F;
                            lastDZ += MathHelper.cos(radians) * 0.2F;
                        }

                    } else f5 = (float) (sprint
                            ? ((double) 0.02F + (double) 0.02F * 0.3D)
                            : 0.02F);

                    if (data.isNewerThan12()) {
                        Vec3 result = getInputVector(new Vec3(strafe, 0, forward), f5, yaw);
                        lastDX += result.xCoord;
                        lastDZ += result.zCoord;
                    } else {
                        Vec2f result = moveFlying(forward, strafe, f5, yaw);
                        lastDX += result.x;
                        lastDZ += result.y;
                    }
                } else {

                    float accel = 0.02F;

                    float f3 = Math.min(3.0F, (float) data.getDepthStriderLevel());

                    if (!onGround) f3 *= 0.5F;

                    if (f3 > 0.0F) {
                        accel += (((float)moveSpeed) * 1.0F - accel) * f3 / 3.0F;
                    }

                    f5 = accel;

                    if (data.isNewerThan12()) {
                        Vec3 result = getInputVector(new Vec3(strafe, 0, forward), f5, yaw);
                        lastDX += result.xCoord;
                        lastDZ += result.zCoord;
                    } else {
                        Vec2f result = moveFlying(forward, strafe, f5, yaw);
                        lastDX += result.x;
                        lastDZ += result.y;
                    }
                }

                double moveDiff = MathUtil.hypot(
                        data.deltas.deltaX - lastDX,
                        data.deltas.deltaZ - lastDZ
                );

                if (test) {
                    if (moveDiff < testLowest) {
                        testLowest = moveDiff;
                        testOutputX = lastDX;
                        testOutputZ = lastDZ;

                        if (testLowest < 1e-15) {
                            return true;
                        }
                    }
                } else {
                    if (moveDiff < lowestMatch) {
                        this.lowestMatch = moveDiff;
                        this.outputX = lastDX;
                        this.outputZ = lastDZ;
                        this.outputXZ = MathUtil.hypot(outputX, outputZ);

                        this.sprinting = sprint;
                        this.sneak = sneaking;
                        this.jumped = jump;
                        this.attacking = attack;
                        this.useItem = using;
                        this.attributeSpeed = (float) moveSpeed;
                        this.moveForward = forward;
                        this.moveStrafe = strafe;
                        this.f5 = f5;
                        this.blockFriction = friction;

                        if (data.elapsed(data.getLastSneakEdge()) <= 3) {
                            if (sneaking || data.isSneaking()) {
                                this.edgeSneakTick = data.getTotalTicks();
                            }
                        }

                        if (this.lowestMatch < 1e-15) {
                            //Bukkit.broadcastMessage(ChatColor.RED + "YURR");
                            //return true;
                        }
                    }
                }

                ++scenarioAmount;
            }
        }
        return false;
    }

    private Vec2f moveFlying(float forward, float strafe, float f5, float yaw) {
        float inputForce = forward * forward + strafe * strafe;

        if (inputForce >= 1.0E-4F) {

            inputForce = MathHelper.sqrt_float(inputForce);

            if (inputForce < 1.0F) {
                inputForce = 1.0F;
            }

            inputForce = f5 / inputForce;

            forward *= inputForce;
            strafe *= inputForce;

            final float yawShit = yaw * (float) Math.PI / 180.F;

            final float yawSin = MathHelper.sin(yawShit);
            final float yawCos = MathHelper.cos(yawShit);

            return new Vec2f((strafe * yawCos - forward * yawSin), (forward * yawCos + strafe * yawSin));
        }
        return new Vec2f(0,0);
    }

    private Vec3 getInputVector(Vec3 inputs, float f5, float yaw) {
        double inputForce = inputs.lengthSqr();

        if (inputForce >= 1.0E-4F) {
            Vec3 vec3 = (inputForce > 1.0D ? inputs.normalizeModern() : inputs).scale(f5);
            float yawSin = MathHelper.sin(yaw * ((float) Math.PI / 180F));
            float yawCos = MathHelper.cos(yaw * ((float) Math.PI / 180F));
            return new Vec3(vec3.xCoord * (double) yawCos - vec3.zCoord * (double) yawSin,
                    0,
                    vec3.zCoord * (double) yawCos + vec3.xCoord * (double) yawSin);
        } else {
            return Vec3.ZERO;
        }
    }
}
