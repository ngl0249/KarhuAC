package me.liwk.karhu.event;


import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import lombok.Getter;
import org.bukkit.util.Vector;


@Getter
public class DigEvent extends Event {

    private final Vector blockPos;
    private final short direction;
    private final DiggingAction digType;
    private final long now;

    public DigEvent(Vector blockPos, short direction, DiggingAction digType, long now) {
        this.blockPos = blockPos;
        this.direction = direction;
        this.digType = digType;
        this.now = now;
    }

}
