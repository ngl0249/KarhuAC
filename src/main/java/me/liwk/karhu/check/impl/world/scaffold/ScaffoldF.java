package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;

@CheckInfo(name = "Scaffold (F)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldF extends PacketCheck {

    public ScaffoldF(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof BlockPlaceEvent) {
            if (((BlockPlaceEvent) packet).isUsableItem()) return;
            if (data.deltas.deltaPitch < 0.7 && data.deltas.deltaYaw > 100 && data.deltas.deltaXZ > 0.2 && data.isNotGroundBridging()) {
                if (++violations > 5) {
                    fail("* Weird stuff" +
                            "\n §f* deltaPitch | deltaYaw §b" + data.deltas.deltaPitch + " | " + data.deltas.deltaYaw +
                            "\n §f* deltaXZ: §b" + data.deltas.deltaXZ, 120);
                }
            } else {
                decrease(0.25D);
            }
        }
    }
}

