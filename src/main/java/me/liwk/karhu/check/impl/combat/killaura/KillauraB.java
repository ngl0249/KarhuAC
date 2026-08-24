package me.liwk.karhu.check.impl.combat.killaura;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;

@CheckInfo(name = "Killaura (B)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false)
public final class KillauraB extends PacketCheck {

    private boolean sentInteract;
    private boolean sentAttack;

    public KillauraB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (!data.isNewerThan8()) {
            if (packet instanceof FlyingEvent) {
                sentInteract = false;
                sentAttack = false;
            } else if (packet instanceof AttackEvent) {
                sentAttack = true;
            } else if (packet instanceof InteractEvent) {
                sentInteract = true;
            } else if (packet instanceof BlockPlaceEvent) {
                if ((sentAttack && !sentInteract) && data.getLastTarget() != -696969) {
                    if (++violations > 1) {
                        fail("* Illegal block order", getBanVL(), 60);
                    }
                } else {
                    violations = Math.max(violations - 0.1, 0);
                }
            }
        }
    }
}
