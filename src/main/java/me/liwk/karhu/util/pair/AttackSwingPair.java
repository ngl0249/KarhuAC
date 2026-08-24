package me.liwk.karhu.util.pair;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttackSwingPair {
    final Long attackTime;
    final Long swingTime;

    public AttackSwingPair(Long attackTime, Long swingTime) {
        this.attackTime = attackTime;
        this.swingTime = swingTime;
    }

    @Override
    public String toString() {
        return swingTime + "," + (attackTime != null ? attackTime : "null");
    }
}
