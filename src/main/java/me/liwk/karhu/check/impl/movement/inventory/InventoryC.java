package me.liwk.karhu.check.impl.movement.inventory;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;

@CheckInfo(name = "Inventory (C)", category = Category.MOVEMENT, subCategory = SubCategory.INVENTORY, experimental = true)
public final class InventoryC extends PacketCheck {


    public InventoryC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {

    }
}
