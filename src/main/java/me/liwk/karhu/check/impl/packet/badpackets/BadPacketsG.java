package me.liwk.karhu.check.impl.packet.badpackets;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.HeldItemSlotEvent;

@CheckInfo(name = "BadPackets (G)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false)
public final class BadPacketsG extends PacketCheck {

    private int slots;

    public BadPacketsG(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(packet instanceof HeldItemSlotEvent) {
            ++slots;
        } else if (packet instanceof FlyingEvent) {
            int threshold = data.isNewerThan8() ? 40 : 10;
            if(slots > threshold) {
                fail("* Sent too many slot packets\n * S §b" + slots, getBanVL(), 110);
            }
            slots = 0;
        }
    }
}
