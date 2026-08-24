package me.liwk.karhu.check.impl.packet.badpackets;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.TransactionEvent;

@CheckInfo(name = "BadPackets (I)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = true)
public final class BadPacketsI extends PacketCheck {
    private long lastFlag;

    public BadPacketsI(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(packet instanceof TransactionEvent) {
            long time = (long) (((TransactionEvent) packet).getNow() / 1E6);
            long flying = (long) (data.lastFlying / 1E6);
            if (time - flying > (data.isNewerThan8() ? 40000 : 4000) + data.getTransactionPing()
                    && time - lastFlag > 250L && !data.isSpectating() && !data.getBukkitPlayer().isDead()) {
                if(++violations > 20) {
                    fail("* Blink?\n T: §b" + (time - flying) / 50L, getBanVL(), 110);
                    lastFlag = time;
                }
            } else {
                violations *= 0.2;
            }
        }
    }
}
