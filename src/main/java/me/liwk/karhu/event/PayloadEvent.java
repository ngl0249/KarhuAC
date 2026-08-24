package me.liwk.karhu.event;

import lombok.Getter;

@Getter
public class PayloadEvent extends Event {

    private final String tag;
    private final byte[] data;

    public PayloadEvent(String x, byte[] y) {
        this.tag = x;
        this.data = y;
    }
}
