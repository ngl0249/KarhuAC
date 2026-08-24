package me.liwk.karhu.event;

import lombok.Getter;
import org.bukkit.util.Vector;

@Getter
public class PositionEvent extends Event {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public PositionEvent(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vector getPositionVector() {
        return new Vector(x, y, z);
    }

}
