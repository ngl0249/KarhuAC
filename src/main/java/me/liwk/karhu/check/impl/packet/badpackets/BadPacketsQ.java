package me.liwk.karhu.check.impl.packet.badpackets;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;

@CheckInfo(name = "BadPackets (Q)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false)
public final class BadPacketsQ extends PacketCheck {


    public BadPacketsQ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof BlockPlaceEvent && Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
            final int face = ((BlockPlaceEvent) packet).getFace();

            final double blockX = ((BlockPlaceEvent) packet).getBlockX(), blockY = ((BlockPlaceEvent) packet).getBlockY(), blockZ = ((BlockPlaceEvent) packet).getBlockZ();

            final boolean invalidX = blockX > 1 || blockX < 0,
                    invalidY = blockY > 1 || blockY < 0,
                    invalidZ = blockZ > 1 || blockZ < 0;

            if(invalidX || invalidY || invalidZ) {
                fail("* Invalid blockplace\n §f* FACE: §b" + face + "\n §f* SUM2: §b" + (blockX + blockY + blockZ), getBanVL(), 310);
            }
        }
    }
}
