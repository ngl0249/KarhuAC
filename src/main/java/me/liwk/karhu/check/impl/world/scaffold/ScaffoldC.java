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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Scaffold (C)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = false)
public final class ScaffoldC extends PacketCheck {

    private int delay, lastDelay, susClicks, clicks;

    public ScaffoldC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
        this.setSetback(false);
    }

    @Override
    public void handle(final Event packet) {

        if (packet instanceof BlockPlaceEvent) {

            BlockPlaceEvent place = ((BlockPlaceEvent) packet);

            if(place.isUsableItem() || data.isRiding()) return;

            Location blockPos = place.get420Johannes();

            Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(blockPos);

            if(block != null) {

                ItemStack stack = place.getItemStack() == null ? new ItemStack(Material.AIR) : place.getItemStack();

                boolean additionable = block.getType() == stack.getType();

                if(additionable) {
                    if (delay <= 8) {
                        if (delay == lastDelay) {
                            ++susClicks;
                        }
                        if (++clicks == 100) {

                            if (susClicks > 70) {
                                fail("* Scaffold like click pattern\n§f* SU §b" + susClicks, getBanVL(), 250L);
                            }

                            susClicks = 0;
                            clicks = 0;
                        }

                    }
                }
            }

            lastDelay = delay;
            delay = 0;
        } else if (packet instanceof FlyingEvent) {
            delay++;
        }
    }
}
