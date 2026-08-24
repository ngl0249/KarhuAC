package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Queue;

@CheckInfo(name = "Scaffold (Q)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldQ extends PacketCheck {

    private final Queue<Integer> delays = new LinkedList<>();

    private int movements;

    public ScaffoldQ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
        this.setSetback(false);
    }

    @Override
    public void handle(final Event packet) {

        if (data.isNewerThan16()) return;

        if (packet instanceof BlockPlaceEvent) {
            BlockPlaceEvent place = ((BlockPlaceEvent) packet);

            Location blockPos = place.get420Johannes();
            Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(blockPos);

            if (block != null) {

                boolean additionable = block.getType() == place.getItemStack().getType() && data.isNotGroundBridging();
                if (movements != 4 && movements < 10 && additionable) {
                    ItemStack item = ((BlockPlaceEvent) packet).getItemStack();
                    if (item != null && item.getType().isBlock()) {
                        if (delays.add(movements) && delays.size() == 50) {
                            double avg = MathUtil.getAverage(delays);
                            double stDev = MathUtil.getStandardDeviation(delays);

                            double cps = 20.0 / avg;

                            if (avg < 1.25 && stDev < 0.075) {

                                String info = String.format("CPS %.3f AVG %s STD %s", cps, avg, stDev);

                                fail("* Rightclicker" +
                                        "\n" + info, getBanVL(), 250L);
                            }

                            delays.clear();
                        }
                    }
                }
            }

            movements = 0;
        } else if (packet instanceof FlyingEvent) {
            ++movements;
        }
    }
}
