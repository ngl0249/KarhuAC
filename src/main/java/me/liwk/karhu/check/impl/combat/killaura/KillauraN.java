package me.liwk.karhu.check.impl.combat.killaura;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.TransactionEvent;

@CheckInfo(name = "Killaura (N)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = true)
public final class KillauraN extends PacketCheck {

    public int targetAmount;
    public int lastEntity;

    public KillauraN(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof AttackEvent) {

            int currentTarget = ((AttackEvent) packet).getEntityId();

            if(currentTarget != lastEntity) {
                ++targetAmount;
            }

            lastEntity = currentTarget;
        } else if(packet instanceof FlyingEvent) {

            if(data.isPossiblyTeleporting()) {
                targetAmount = 0;
                return;
            }

            if(data.getClientVersion().getProtocolVersion() <= 47) {

                if (targetAmount > 1) {
                    fail("* Multiaura"
                                    + "\n §f* targets: §b" + targetAmount
                                    + "\n §f* cps: §b" + format(3, data.getCps()),
                            getBanVL(), 300L);
                }
            }
            targetAmount = 0;
        } else if(packet instanceof TransactionEvent) {

            if(data.getClientVersion().getProtocolVersion() > 47) {

                if(targetAmount > 1) {
                    fail("* hMultiaura (1.9)"
                                    + "\n §f* targets: §b" + targetAmount
                                    + "\n §f* cps: §b" + format(3, data.getCps()),
                            getBanVL(), 300L);
                }

                targetAmount = 0;
            }
        }
    }
}
