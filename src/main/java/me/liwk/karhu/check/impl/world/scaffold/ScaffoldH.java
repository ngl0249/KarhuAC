package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;

@CheckInfo(name = "Scaffold (H)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = false)
public final class ScaffoldH extends PacketCheck {

    public ScaffoldH(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof BlockPlaceEvent) {
            if (((BlockPlaceEvent) packet).isUsableItem() || data.isPossiblyTeleporting() || !data.isNotGroundBridging()) return;
            if (data.deltas.deltaPitch > 15 && data.deltas.deltaYaw == 0 && data.deltas.deltaXZ > 0.2) {
                if (++violations > 4) {
                    fail("* Weird stuff" +
                            "\n §f* deltaPitch: §b" + data.deltas.deltaPitch +
                            "\n §f* deltaXZ: §b" + data.deltas.deltaXZ, getBanVL(), 120);
                }
            } else {
                decrease(0.25);
            }
        }
    }
}

