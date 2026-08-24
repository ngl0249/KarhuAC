package me.liwk.karhu.check.impl.packet.badpackets;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.ActionEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;

@CheckInfo(name = "BadPackets (R)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false)
public final class BadPacketsR extends PacketCheck {

    private int sprints;

    public BadPacketsR(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(data.isNewerThan8()) return;
        if(packet instanceof FlyingEvent) {
            if(sprints > 1 && !data.isPossiblyTeleporting()) {
                fail("* Too many actions", 300L);
            }
            sprints = 0;
        } else if(packet instanceof ActionEvent) {
            if(((ActionEvent) packet).getAction().equals(WrapperPlayClientEntityAction.Action.START_SPRINTING)) {
                ++sprints;
            }
            if(((ActionEvent) packet).getAction().equals(WrapperPlayClientEntityAction.Action.STOP_SPRINTING)) {
                ++sprints;
            }
        }
    }
}
