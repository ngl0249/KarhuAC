package me.liwk.karhu.data;

public final class Deltas {

    public double deltaX, deltaZ, motionY,
            lastMotionY, lastLastMotionY,
            deltaXZ, lastDXZ, lastLastDXZ,
            lastDX, lastDZ,
            accelXZ, lastAccelXZ;

    public double deltaXKb, deltaYKb, deltaZKb, lastDeltaXKb, lastDeltaYKb, lastDeltaZKb;
    public double predictX, predictY, predictZ;
    public float deltaYaw, deltaPitch;
    public float lDeltaYaw, lDeltaPitch;

}
