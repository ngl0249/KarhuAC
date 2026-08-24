package me.liwk.karhu.event;

import org.bukkit.entity.Entity;

public class SteerEvent extends Event {
    private final boolean unmount;

    public SteerEvent(boolean unmount) {
        this.unmount = unmount;
    }

    public boolean isUnmount() {
        return this.unmount;
    }
}
