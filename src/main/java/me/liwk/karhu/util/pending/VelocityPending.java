package me.liwk.karhu.util.pending;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.util.Vector;


@RequiredArgsConstructor
@Getter
public class VelocityPending {

    private boolean markedSent;
    private final short id;
    private final Vector velocity;
    private final boolean relative;
    private final int sequence;

    public void markSent() {
        this.markedSent = true;
    }

}
