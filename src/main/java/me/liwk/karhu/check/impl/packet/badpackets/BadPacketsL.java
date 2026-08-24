package me.liwk.karhu.check.impl.packet.badpackets;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;

@CheckInfo(name = "BadPackets (L)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false)
public final class BadPacketsL extends PacketCheck {

    public BadPacketsL(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

    }
}

