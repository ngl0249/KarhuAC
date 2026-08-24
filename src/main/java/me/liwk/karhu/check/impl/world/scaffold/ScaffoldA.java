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
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Scaffold (A)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = false)
public final class ScaffoldA extends PacketCheck {

    private long lastFlying;

    public ScaffoldA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof BlockPlaceEvent) {
            BlockPlaceEvent place = ((BlockPlaceEvent) packet);

            final ItemStack stack = place.getItemStack();
            final long diff = place.getTimeMillis() - lastFlying;

            if (stack != null) {
                if (stack.getType().isBlock() && !data.isPossiblyTeleporting()) {
                    if (diff < 10L && !data.isLagging(data.getTotalTicks())
                            && data.elapsed(data.getLastPacketDrop()) > 5
                            && !this.getKarhu().isServerLagging(place.getTimeMillis())
                            && this.getKarhu().getTPS() >= 19.98) {
                        if (++violations > 8) {
                            fail("* Irregular place\n §f* delta: §b" + diff + "\n §f* deltaXZ: §b" + data.deltas.deltaXZ, getBanVL(), 120);
                        }
                    } else {
                        decrease(0.8);
                    }
                }
            }
        } else if (packet instanceof FlyingEvent) {
            lastFlying = ((FlyingEvent) packet).getCurrentTimeMillis();
        }
    }
}
