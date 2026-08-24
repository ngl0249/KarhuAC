package me.liwk.karhu.check.impl.movement.inventory;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.WindowEvent;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.entity.Player;

@CheckInfo(name = "Inventory (A)", category = Category.MOVEMENT, subCategory = SubCategory.INVENTORY, experimental = true)
public final class InventoryA extends PacketCheck {

    public InventoryA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {
        if (packet instanceof WindowEvent) {

            if (data.elapsed(data.getLastPistonPush()) <= 3) return;

            double offsetH = data.deltas.deltaXZ, lastOffsetH = data.deltas.lastDXZ;

            if (offsetH - lastOffsetH >= 0.0 && offsetH > 0.1
                    && !data.isAllowFlying() && !data.isPossiblyTeleporting()
                    && data.elapsed(data.getLastGlide()) > 20
                    && data.getVelocityHorizontal() == 0) {
                if (++violations > (data.isSprinting() ? 2 : 4)) {
                    fail("* Moving while clicking inventory slots" +
                            "\n §f* deltaXZ §b" + offsetH, getBanVL(), 300);

                    if (violations > (data.isSprinting() ? 3 : 5)) {
                        Player player = data.getBukkitPlayer();
                        if (player != null) {
                            Tasker.run(player::closeInventory);
                            MiscellaneousAlertPoster.postMitigation(data, violations,
                                    "Inventory (A)",
                                    "* Clicking inventory and moving" +
                                            "\n §f* deltaXZ §b" + offsetH
                            );

                        }
                    }
                }
            } else {
                violations = Math.max(violations - 0.5, 0);
            }
        }
    }
}
