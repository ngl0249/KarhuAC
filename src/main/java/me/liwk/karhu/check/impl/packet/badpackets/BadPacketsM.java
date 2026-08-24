package me.liwk.karhu.check.impl.packet.badpackets;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.ActionEvent;
import me.liwk.karhu.event.DigEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckInfo(name = "BadPackets (M)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = true, credits = "§c§lCREDITS: §aWizzard §7made this check.")
public final class BadPacketsM extends PacketCheck {

    Deque<Integer> interactions = new ArrayDeque<>();
    int flying;
    boolean released;

    public BadPacketsM(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (data.getClientVersion().getProtocolVersion() > 47) return;

        if (packet instanceof FlyingEvent) {
            if (((FlyingEvent) packet).hasMoved() || ((FlyingEvent) packet).hasLooked()) {
                ++flying;
                released = false;
            }

        } else if (packet instanceof ActionEvent) {
            if (((ActionEvent) packet).getAction() != WrapperPlayClientEntityAction.Action.START_SPRINTING) return;
            if (!released && data.getLastAttackTick() <= 60) {
                if (interactions.add(flying) && interactions.size() >= 30) {

                    double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(interactions));
                    if (std < 0.3)
                        fail(String.format("* Wtap\nstd: %.2f", std), getBanVL(), 125L);

                    interactions.clear();
                }
            }
            flying = 0;
        } else if (packet instanceof DigEvent) {
            if (((DigEvent) packet).getDigType() == DiggingAction.RELEASE_USE_ITEM) {
                released = true;
            }
        }

    }

}
