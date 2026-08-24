package me.liwk.karhu.event;

import lombok.Getter;

@Getter
public class AttackEvent extends Event {

    private final int entityId, recordAttack;
    private final long now;
    private final long timeMillis;

    private final boolean player;

    public AttackEvent(int entityId, boolean player, long now, long timeMillis, int recordAttack) {
        this.entityId = entityId;
        this.player = player;
        this.now = now;
        this.timeMillis = timeMillis;
        this.recordAttack = recordAttack;
    }

}
