package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.util.MathUtil;

@CheckInfo(name = "Scaffold (G)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldG extends PacketCheck {
    private double lastDeltaPitch;

    public ScaffoldG(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof BlockPlaceEvent) {
            if(!((BlockPlaceEvent) packet).isUsableItem()) {

                boolean validY = MathUtil.isNearlySame(data.getLocation().y, ((BlockPlaceEvent) packet).getBlockPos().getBlockY(), 2);

                if (data.deltas.deltaPitch == lastDeltaPitch && data.deltas.deltaYaw == 0
                        && data.deltas.deltaXZ > 0.21 && data.deltas.deltaPitch > 1
                        && validY) {
                    if (++violations > 35) {
                        fail("* Weird stuff" +
                                        "\n §f* deltaPitch: §b" + data.deltas.deltaPitch +
                                        "\n §f* deltaXZ: §b" + data.deltas.deltaXZ,
                                getBanVL(), 120);
                    }
                } else {
                    decrease(0.35);
                }
                lastDeltaPitch = data.deltas.deltaPitch;
            }
        }
    }
}

