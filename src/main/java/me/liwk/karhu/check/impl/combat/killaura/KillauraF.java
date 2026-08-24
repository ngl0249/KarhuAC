package me.liwk.karhu.check.impl.combat.killaura;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;

@CheckInfo(name = "Killaura (F)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false)
public final class KillauraF extends PacketCheck {

    public KillauraF(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof AttackEvent && (data.isPlacing() || (data.isBlocking() && data.getBukkitPlayer().isBlocking()))) {
            fail("* Illegal sword blocking order"
                    + "\n §f* P: §b" + data.isPlacing()
                    + "\n §f* B: §b" + data.isBlocking(), getBanVL(), 600L);
        }
    }
}
