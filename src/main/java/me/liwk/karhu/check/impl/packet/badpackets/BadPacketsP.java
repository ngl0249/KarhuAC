package me.liwk.karhu.check.impl.packet.badpackets;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;

@CheckInfo(name = "BadPackets (P)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false)
public final class BadPacketsP extends PacketCheck {

    private float lpitch, lyaw;

    public BadPacketsP(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(packet instanceof FlyingEvent) {
            if (((FlyingEvent) packet).hasLooked()) {
                if ((((FlyingEvent) packet).getPitch() == lpitch) && (((FlyingEvent) packet).getYaw() == lyaw) && !data.isPossiblyTeleporting()) {
                    fail("* Improper pitch\n §f* P: §b" + this.format(0, ((FlyingEvent) packet).getPitch()), getBanVL(), 110);
                }
                lpitch = ((FlyingEvent) packet).getPitch();
                lyaw = ((FlyingEvent) packet).getYaw();
            }
        }
    }
}