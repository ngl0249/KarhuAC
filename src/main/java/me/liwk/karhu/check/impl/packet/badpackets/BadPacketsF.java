package me.liwk.karhu.check.impl.packet.badpackets;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.SteerEvent;

@CheckInfo(name = "BadPackets (F)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false, credits = "§c§lCREDITS: §aOilSlug §7for the base idea.")
public final class BadPacketsF extends PacketCheck {

    private double ticks;
    private boolean sent;

    public BadPacketsF(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(packet instanceof SteerEvent && data.getTotalTicks() > 200) {
            sent = !((SteerEvent) packet).isUnmount();
        } else if(packet instanceof FlyingEvent) {
            if(sent) {
                if (++ticks > 3) {
                    if (data.getBukkitPlayer().getVehicle() == null && !data.isRiding() && !data.isExitingVehicle()) {
                        if (++violations > 5) {
                            fail("* Sent vehicle packet without being inside a vehicle", getBanVL(), 110);
                        }
                    } else {
                        decrease(0.75);
                    }
                }
                sent = false;
            } else {
                ticks = 0;
            }
        }
    }
}
