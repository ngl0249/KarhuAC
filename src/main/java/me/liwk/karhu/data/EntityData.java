package me.liwk.karhu.data;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import lombok.Setter;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.handler.collision.CollisionBoxParser;
import me.liwk.karhu.handler.collision.enums.Boxes;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntityData {

    public double minX, minY, minZ;
    public double maxX, maxY, maxZ;

    public double lastMinX, lastMaxX, lastMinY;
    public double lastMaxY, lastMinZ, lastMaxZ;

    public double newX, newY, newZ;

    public boolean uncertainBox = false, riding;

    public float height = 1.8F, width = 0.6F;

    public int posIncrements;
    public int originalPosIncrements = 0; // NEW: Track original increment count

    public int cancelledLerpSteps = 0;

    // Store the starting position when lerp is cancelled
    private double lerpStartMinX, lerpStartMaxX, lerpStartMinY;
    private double lerpStartMaxY, lerpStartMinZ, lerpStartMaxZ;

    public List<Vector3d> newLocations = new ArrayList<>();

    public Vector lastUpdate;

    public int flyingsBetween;

    public int eid, vehicleId;

    public long creationTime;

    public EntityType type;

    public int hookAttachedToId;
    public int owner;

    public boolean isBoat, gravity;

    public float scale = 1;

    public EntityData(double newX, double newY, double newZ, int eid, EntityType type, int owner) {

        this.newX = newX;
        this.newY = newY;
        this.newZ = newZ;

        this.newLocations.add(new Vector3d(newX, newY, newZ));

        this.minX = newX;
        this.minY = newY;
        this.minZ = newZ;

        this.maxX = minX;
        this.maxY = minY;
        this.maxZ = minZ;

        this.posIncrements = 0;
        this.eid = eid;

        this.creationTime = Karhu.getInstance().getServerTick();
        this.type = type;
        this.owner = owner;

        if (EntityTypes.isTypeInstanceOf(type, EntityTypes.BOAT)) {
            isBoat = true;
        }
    }

    public void interpolate() {
        if (posIncrements > 0) {
            double newMinX = Double.MAX_VALUE, newMinY = Double.MAX_VALUE, newMinZ = Double.MAX_VALUE;
            double newMaxX = -Double.MAX_VALUE, newMaxY = -Double.MAX_VALUE, newMaxZ = -Double.MAX_VALUE;

            for (Vector3d vector : newLocations) {
                newMinX = Math.min(vector.getX(), newMinX);
                newMinY = Math.min(vector.getY(), newMinY);
                newMinZ = Math.min(vector.getZ(), newMinZ);
                newMaxX = Math.max(vector.getX(), newMaxX);
                newMaxY = Math.max(vector.getY(), newMaxY);
                newMaxZ = Math.max(vector.getZ(), newMaxZ);
            }

            this.updateLast();

            // MODIFIED: Handle cancelled lerp by expanding box to include both scenarios
            /*if (cancelledLerpSteps > 0) {
                // On affected versions, the client IMMEDIATELY snaps to target position
                // But we're uncertain about timing, so expand box to include:
                // 1. The target position (where client snapped to)
                // 2. Current interpolated position (in case we're wrong about timing)

                double targetMinX = newMinX;
                double targetMaxX = newMaxX;
                double targetMinY = newMinY;
                double targetMaxY = newMaxY;
                double targetMinZ = newMinZ;
                double targetMaxZ = newMaxZ;

                // Normal interpolation step
                double interpMinX = minX + (newMinX - minX) / (double)posIncrements;
                double interpMaxX = maxX + (newMaxX - maxX) / (double)posIncrements;
                double interpMinY = minY + (newMinY - minY) / (double)posIncrements;
                double interpMaxY = maxY + (newMaxY - maxY) / (double)posIncrements;
                double interpMinZ = minZ + (newMinZ - minZ) / (double)posIncrements;
                double interpMaxZ = maxZ + (newMaxZ - maxZ) / (double)posIncrements;

                // Expand box to include BOTH target and interpolated positions
                minX = Math.min(targetMinX, interpMinX);
                maxX = Math.max(targetMaxX, interpMaxX);
                minY = Math.min(targetMinY, interpMinY);
                maxY = Math.max(targetMaxY, interpMaxY);
                minZ = Math.min(targetMinZ, interpMinZ);
                maxZ = Math.max(targetMaxZ, interpMaxZ);

                // Don't reset lerpCancelled until interpolation is complete
                if (posIncrements <= 1) {
                    cancelledLerpSteps = 0;
                    originalPosIncrements = 0;
                }
            } else {*/
                // Normal interpolation
                minX += (newMinX - minX) / (double)posIncrements;
                maxX += (newMaxX - maxX) / (double)posIncrements;
                minY += (newMinY - minY) / (double)posIncrements;
                maxY += (newMaxY - maxY) / (double)posIncrements;
                minZ += (newMinZ - minZ) / (double)posIncrements;
                maxZ += (newMaxZ - maxZ) / (double)posIncrements;
            //}
        }

        posIncrements--;
    }

    public void cancelInterpolation() {
        this.cancelledLerpSteps = 1;
        this.originalPosIncrements = this.posIncrements;

        // Store current position as the starting point for interpolation calculations
        this.lerpStartMinX = this.minX;
        this.lerpStartMaxX = this.maxX;
        this.lerpStartMinY = this.minY;
        this.lerpStartMaxY = this.maxY;
        this.lerpStartMinZ = this.minZ;
        this.lerpStartMaxZ = this.maxZ;
    }

    public void postTransaction() {
        if (newLocations.size() > (Karhu.getInstance().getConfigManager().isReachSafe() ? 2 : 1)) {
            newLocations.remove(0);
        }

        uncertainBox = newLocations.size() > (Karhu.getInstance().getConfigManager().isReachSafe() ? 2 : 1);
    }

    private void updateLast() {
        lastMinX = minX;
        lastMaxX = maxX;
        lastMinY = minY;
        lastMaxY = maxY;
        lastMinZ = minZ;
        lastMaxZ = maxZ;
    }

    public void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
        }
    }

    public AxisAlignedBB getEntityBoundingBox() {
        Boxes box = CollisionBoxParser.from(type);
        float f = box.getWidth() * scale;
        float f1 = box.getHeight() * scale;
        return new AxisAlignedBB(minX - (double)f, minY, minZ - (double)f,
                maxX + (double)f, maxY + (double)f1, maxZ + (double)f);
    }

    public Vector3d getNewXYZ() {
        return new Vector3d(newX, newY, newZ);
    }

    public AxisAlignedBB getEntityPos() {
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB getEntityBoundingBoxLast() {
        Boxes box = CollisionBoxParser.from(type);
        float f = box.getWidth() * scale;
        float f1 = box.getHeight() * scale;
        return new AxisAlignedBB(lastMinX - (double)f, lastMinY, lastMinZ - (double)f,
                lastMaxX + (double)f, lastMaxY + (double)f1, lastMaxZ + (double)f);
    }

    public long getExist() {
        return Karhu.getInstance().getServerTick() - creationTime;
    }

}
