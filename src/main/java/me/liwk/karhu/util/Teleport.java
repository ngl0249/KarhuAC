package me.liwk.karhu.util;

import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Setter
public class Teleport {

    public final TeleportPosition position;
    public boolean accepted = false;
    public boolean moved = false;
    public final RelativeFlag relativeFlag;
    public final int teleportId;
    public final long timestamp;
}
