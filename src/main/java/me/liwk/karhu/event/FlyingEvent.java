package me.liwk.karhu.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@Setter
public class FlyingEvent extends Event {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean hasMoved;
    private boolean hasLooked;
    private boolean isOnGround;
    private boolean isTeleport;
    private final long nanoTime, currentTimeMillis;

    public FlyingEvent(double x, double y, double z, float yaw, float pitch, boolean hasMoved, boolean hasLooked, boolean isOnGround, boolean isTeleport, long nanoTime, long currentTimeMillis) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.hasMoved = hasMoved;
        this.hasLooked = hasLooked;
        this.isOnGround = isOnGround;
        this.isTeleport = isTeleport;
        this.nanoTime = nanoTime;
        this.currentTimeMillis = currentTimeMillis;
    }


    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean hasLooked() {
        return hasLooked;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

}
