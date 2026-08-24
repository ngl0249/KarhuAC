package me.liwk.karhu.util.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.liwk.karhu.util.location.CustomLocation;

@Getter
@RequiredArgsConstructor
public final class MovementUpdate {
    public final CustomLocation fromFrom, from, to;
    private final boolean ground;
}
