package me.liwk.karhu.event;

import lombok.Getter;

@Getter
public class VelocityEvent extends Event {
    private final double x;
    private final double y;
    private final double z;
    private final int eid;

    public VelocityEvent(double x, double y, double z, int eid) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.eid = eid;
    }
}
