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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Scaffold (I)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldI extends PacketCheck {

    public ScaffoldI(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(packet instanceof BlockPlaceEvent) {


            final ItemStack stack = ((BlockPlaceEvent) packet).getItemStack();
            boolean validBlock = stack != null && stack.getType() != Material.AIR;

            if(validBlock && !((BlockPlaceEvent) packet).isUsableItem()) {
                if(data.deltas.deltaYaw > 1.5F && data.deltas.deltaXZ > 0.12 && MathUtil.isNearlySame(data.deltas.accelXZ, data.deltas.lastAccelXZ, 1E-7)) {

                    if (++violations > 3) {
                        fail("* Not slowing down\n §f* deltaYaw: §b" + data.deltas.deltaYaw + "\n §f* deltaXZ: §b" + data.deltas.deltaXZ + "\n §f* ac: §b" + Math.abs(data.deltas.accelXZ - data.deltas.lastAccelXZ), getBanVL(), 150);
                    }

                } else {
                    violations = Math.max(violations - 0.075, 0);
                }
            }
        }
    }
}
