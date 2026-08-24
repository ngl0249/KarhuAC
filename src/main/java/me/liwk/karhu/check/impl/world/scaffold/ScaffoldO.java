package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.ActionEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action.STOP_SNEAKING;

@CheckInfo(name = "Scaffold (O)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldO extends PacketCheck {

    Deque<Integer> interactions = new ArrayDeque<>();
    int flying;

    public ScaffoldO(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof FlyingEvent) {
            if (((FlyingEvent) packet).hasMoved() || ((FlyingEvent) packet).hasLooked()) {
                ++flying;
            }

        } else if (packet instanceof ActionEvent) {
            if (((ActionEvent) packet).getAction() != STOP_SNEAKING) return;
            if (data.elapsed(data.getUnderPlaceTicks()) > 3 || !data.isNotGroundBridging()) return;

            if (interactions.add(flying) && interactions.size() >= 15) {

                double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(interactions));
                if (std < 0.325) {
                    fail("* Eagle\n" + String.format("std: %.2f", std), getBanVL(), 125L);
                } else if (std < 0.65) {
                    if (++violations > 2) {
                        fail("* Eagle\n" + String.format("std: %.2f", std), getBanVL(), 125L);
                    }
                } else violations = Math.max(violations - 0.35, 0);

                interactions.clear();
            }
            flying = 0;
        }

    }
}
