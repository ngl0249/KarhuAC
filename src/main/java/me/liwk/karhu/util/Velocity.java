package me.liwk.karhu.util;

import lombok.RequiredArgsConstructor;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class Velocity {

    private final Vector velocity;
    private final int firstUid, secondUid;

    private boolean accepted, confirmed;
}
