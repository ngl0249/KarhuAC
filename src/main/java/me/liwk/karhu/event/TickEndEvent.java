package me.liwk.karhu.event;

import lombok.Getter;

public class TickEndEvent extends Event {

    @Getter
    private final long nanoTime, timeMillis;

    public TickEndEvent(long nanoTime, long timeMillis) {
        this.nanoTime = nanoTime;
        this.timeMillis = timeMillis;
    }


}

