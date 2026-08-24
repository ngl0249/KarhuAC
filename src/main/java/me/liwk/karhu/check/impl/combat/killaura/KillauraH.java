package me.liwk.karhu.check.impl.combat.killaura;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;

@CheckInfo(name = "Killaura (H)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false)
public final class KillauraH extends PacketCheck {

    private boolean sentDig;
    private boolean sentPlace;

    public KillauraH(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (!data.isNewerThan8()) {
            if (packet instanceof FlyingEvent) {
                sentDig = false;
                sentPlace = false;
            } else if (packet instanceof AttackEvent) {
                if (!sentPlace & sentDig) {
                    if (++violations > 1) {
                        fail("* Illegal block order", getBanVL(), 60);
                    }
                } else {
                    violations = Math.max(violations - 0.1, 0);
                }
            } else if (packet instanceof DigEvent) {
                DiggingAction type = ((DigEvent) packet).getDigType();
                if (type != DiggingAction.DROP_ITEM_STACK && type != DiggingAction.DROP_ITEM) {
                    sentDig = true;
                }
            } else if (packet instanceof BlockPlaceEvent) {
                sentPlace = true;
            }
        }
    }
}
