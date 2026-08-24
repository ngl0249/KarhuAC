package me.liwk.karhu.event;

import lombok.Getter;

@Getter
public class TransactionEvent extends Event {
    private final long now;

    public TransactionEvent(long now) {
        this.now = now;
    }
}
