package me.liwk.karhu.event;

import com.github.retrooper.packetevents.util.Vector3f;
import lombok.Getter;
import org.bukkit.entity.Entity;

@Getter
public class InteractEvent extends Event {

    private final int entityId;
    private final boolean isPlayer;
    private final Vector3f vec3D;
    private final boolean at;
    private final long now;

    public InteractEvent(int entityId, boolean isPlayer, Vector3f vec3D, boolean at, long now) {
        this.entityId = entityId;
        this.isPlayer = isPlayer;
        this.vec3D = vec3D;
        this.at = at;
        this.now = now;
    }

}
