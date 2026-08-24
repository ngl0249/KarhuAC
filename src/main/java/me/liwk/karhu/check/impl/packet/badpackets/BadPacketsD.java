package me.liwk.karhu.check.impl.packet.badpackets;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;

@CheckInfo(name = "BadPackets (D)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false)
public final class BadPacketsD extends PacketCheck {

    public BadPacketsD(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(packet instanceof FlyingEvent) {
            if(data.getTotalTicks() < 300) {
                return;
            }

            if(data.getPing() == 0 && data.getTransactionPing() > 1 || data.getPing() > 1 && data.getTransactionPing() == 0) {
                if(violations > 5) {
                    fail("§b* §fNull ping\n§b* §fKPing=§b" + data.getPing() + "\n§b* §fTPing=§b" + data.getTransactionPing(), getBanVL(), 110);
                }
            } else {
                violations = 0;
            }
        }
    }
}
