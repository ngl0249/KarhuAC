package me.liwk.karhu.handler;

import lombok.RequiredArgsConstructor;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.world.nms.FrictionLookup;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public final class MovementHandler {

    private final KarhuPlayer data;

    public void handleMotions(boolean moved, boolean look) {

        if (look) {
            this.data.deltas.lDeltaPitch = this.data.deltas.deltaPitch;
            this.data.deltas.lDeltaYaw = this.data.deltas.deltaYaw;
            this.data.deltas.deltaPitch = Math.abs(this.data.getLocation().pitch - this.data.getLastLocation().pitch);
            this.data.deltas.deltaYaw = calculateYawDelta(
                    this.data.getLocation().yaw,
                    this.data.getLastLocation().yaw
            );
        } else {
            this.data.deltas.lDeltaPitch = this.data.deltas.deltaPitch;
            this.data.deltas.lDeltaYaw = this.data.deltas.deltaYaw;
            this.data.deltas.deltaPitch = 0;
            this.data.deltas.deltaYaw = 0;
        }

        if (data.getPositionPackets() > 1) {
            this.data.deltas.lastLastMotionY = this.data.deltas.lastMotionY;
            this.data.deltas.lastMotionY = this.data.deltas.motionY;

            this.data.deltas.lastDX = this.data.deltas.deltaX;
            this.data.deltas.lastDZ = this.data.deltas.deltaZ;

            this.data.deltas.lastDeltaXKb = this.data.deltas.deltaX;
            this.data.deltas.lastDeltaYKb = this.data.deltas.motionY;
            this.data.deltas.lastDeltaZKb = this.data.deltas.deltaZ;

            this.data.deltas.deltaX = this.data.getLocation().x - this.data.getLastLocation().x;
            this.data.deltas.deltaZ = this.data.getLocation().z - this.data.getLastLocation().z;

            this.data.deltas.motionY = this.data.getLocation().y - this.data.getLastLocation().y;

            this.data.deltas.deltaXKb = this.data.deltas.deltaX;
            this.data.deltas.deltaYKb = this.data.deltas.motionY;
            this.data.deltas.deltaZKb = this.data.deltas.deltaZ;

            if (data.getTeleportManager().teleportTicks == 0) {
                this.data.deltas.lastLastMotionY = 0;
                this.data.deltas.lastMotionY = 0;
                this.data.deltas.motionY = 0;
            }

            this.data.deltas.lastLastDXZ = this.data.deltas.lastDXZ;
            this.data.deltas.lastDXZ = this.data.deltas.deltaXZ;
            this.data.deltas.deltaXZ = MathUtil.hypot(this.data.deltas.deltaX, this.data.deltas.deltaZ);

            this.data.deltas.lastAccelXZ = this.data.deltas.accelXZ;
            this.data.deltas.accelXZ = data.deltas.deltaXZ - data.deltas.lastDXZ;
        }

        data.setRodPullBox(data.getRodPulls().isEmpty() ? null : new AxisAlignedBB());

        if (data.getRodPulls().isEmpty()) {
            data.setLastRodPullLeniencyXZ(data.getRodPullLeniencyXZ());
            data.setLastRodPullLeniencyY(data.getRodPullLeniencyY());
            data.setRodPullLeniencyXZ(0);
            data.setRodPullLeniencyY(0);
        }

        for (int owner : data.getRodPulls()) {
            EntityData entityData = data.getEntityData().get(owner);
            if (entityData == null) continue;

            AxisAlignedBB entityBox = entityData.getEntityPos();

            Vector maxLocation = new Vector(entityBox.maxX, entityBox.maxY, entityBox.maxZ);
            Vector minLocation = new Vector(entityBox.minX, entityBox.minY, entityBox.minZ);

            Vector diff = minLocation.subtract(new Vector(data.getLastLocation().x,
                    data.getLastLocation().y + 0.8 * 1.8,
                    data.getLastLocation().z)).multiply(0.1);
            data.getRodPullBox().minX = Math.min(0, diff.getX());
            data.getRodPullBox().minY = Math.min(0, diff.getY());
            data.getRodPullBox().minZ = Math.min(0, diff.getZ());

            diff = maxLocation.subtract(new Vector(data.getLastLocation().x,
                    data.getLastLocation().y + 0.8 * 1.8,
                    data.getLastLocation().z)).multiply(0.1);
            data.getRodPullBox().maxX = Math.max(0, diff.getX());
            data.getRodPullBox().maxY = Math.max(0, diff.getY());
            data.getRodPullBox().maxZ = Math.max(0, diff.getZ());

            data.setRodPullLeniencyXZ(data.getRodPullBox().distancesXZ());
            data.setRodPullLeniencyY(data.getRodPullBox().distancesY());
            data.setLastRodPullTick(data.getTotalTicks());

        }

        data.getRodPulls().clear();
    }

    public void handleOther(boolean ground) {

        if (!this.data.isOnGroundServer() && this.data.deltas.motionY < 0.0D && !this.data.isOnLiquid() && !this.data.isInWeb()) {
            this.data.fallDistance = (this.data.fallDistance + (float) -this.data.deltas.motionY);
        } else {
            if (data.isOnGroundServer() && !data.isOnSlime() && !data.isWasOnSlime()) {
                this.data.lFallDistance = this.data.fallDistance;
                this.data.fallDistance = 0.0f;
            }
            if (this.data.isOnLiquid() || this.data.isInWeb()) {
                this.data.lFallDistance = this.data.fallDistance;
                this.data.fallDistance = 0.0f;
            }
        }

        this.data.setLastTickFriction(this.data.getCurrentFriction());
        this.data.setCurrentFriction(FrictionLookup.lookup(this.data));

        //Bukkit.broadcastMessage("FRICT TICK: " + data.getTotalTicks() + " | " + data.getCurrentFriction());

        this.data.setLClientAirTicks(data.getClientAirTicks());

        this.data.setAirTicks(!data.isOnGroundServer() ? this.data.getAirTicks() + 1 : 0);
        this.data.setClientAirTicks(!ground ? this.data.getClientAirTicks() + 1 : 0);

        this.data.setJumpFactor(data.isOnHoney() ? 0.5F : 1.0F);

    }

    public float calculateYawDelta(float currentYaw, float lastYaw) {
        // Normalize both yaw values to the range [0, 360)
        currentYaw = (currentYaw % 360 + 360) % 360;
        lastYaw = (lastYaw % 360 + 360) % 360;

        // Calculate the shortest angular distance
        float delta = Math.abs(currentYaw - lastYaw);

        // If the delta is greater than 180 degrees, take the shorter route around the circle
        if (delta > 180) {
            delta = 360 - delta;
        }

        return delta;
    }
}
