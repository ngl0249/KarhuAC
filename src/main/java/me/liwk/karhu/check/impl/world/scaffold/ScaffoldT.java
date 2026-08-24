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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Scaffold (T)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldT extends PacketCheck {

    private double deltaYaw;
    private int inputX;

    public ScaffoldT(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof FlyingEvent) {
            if (((FlyingEvent) packet).hasLooked()) {
                deltaYaw = data.deltas.deltaYaw;
                inputX = data.getInputX();
            }
        } else if (packet instanceof BlockPlaceEvent) {
            BlockPlaceEvent place = ((BlockPlaceEvent) packet);
            int face = place.getFace();

            ItemStack stack = place.getItemStack() == null ? new ItemStack(Material.AIR) : place.getItemStack();

            if (!place.isUsableItem() && stack.getType().isSolid() && stack.getType().isBlock()) {
                double dividedYaw = Math.abs(deltaYaw - inputX);

                if (deltaYaw > 30 && dividedYaw > 90
                        && data.deltas.deltaXZ > 0.18 && data.deltas.motionY == 0
                        && data.isNotGroundBridging() && face != 1) {
                    if (increase(1) >= 6) {
                        fail("* Noob " + Math.round(deltaYaw * 1000), 300L);
                        decrease(1);
                    }
                } else {
                    decrease(0.1D);
                }
            }

            deltaYaw = 0;
        }
    }
}
