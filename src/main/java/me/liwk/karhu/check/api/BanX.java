package me.liwk.karhu.check.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public final class BanX {
    public String player;
    public String type;
    public long time;
    public String data;
    public long ping;
    public double TPS;
}
