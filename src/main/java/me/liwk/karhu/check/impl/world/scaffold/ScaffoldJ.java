package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.util.Vector;


@CheckInfo(name = "Scaffold (J)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldJ extends PacketCheck {

    public ScaffoldJ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof BlockPlaceEvent) {
            final Vector pos = ((BlockPlaceEvent)packet).getBlockPos();
            if ((pos.getX() != -1 && (pos.getY() != 255.0 || pos.getY() != -1.0) && pos.getZ() != -1.0) && !data.isUsingItem() && data.isPlacing()) {

                double ydiff = pos.getBlockY() - data.getLocation().y;

                boolean invalidX = pos.getX() == data.getLocation().getX();
                boolean invalidY = ydiff > 0.0 && ydiff < 1;
                boolean invalidZ = pos.getZ() == data.getLocation().getZ();

                if(invalidX && invalidY && invalidZ && !data.isOnClimbable() && !data.isNearClimbable()) {
                    fail("* Placing block inside\n §f* blockY: §b" + pos.getY() + "\n §f* playerY: §b" + data.getLocation().y, getBanVL(), 120);
                }
            }

        }
    }
}
