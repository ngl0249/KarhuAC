package me.liwk.karhu.event;

import lombok.Getter;

@Getter
public class VehicleEvent extends Event {
    private final double x;
    private final double y;
    private final double z;

    public VehicleEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
