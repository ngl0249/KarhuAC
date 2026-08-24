package me.liwk.karhu.data.combat;

import lombok.Getter;

@Getter
public class CombatData {
    private final double noninterpolatedDist;
    private final double interpolatedDist;
    private final long ping;
    private final long timestamp;

    public CombatData(double noninterpolatedDist, double interpolatedDist, long ping, long timestamp) {
        this.noninterpolatedDist = noninterpolatedDist;
        this.interpolatedDist = interpolatedDist;
        this.ping = ping;
        this.timestamp = timestamp;
    }

}
