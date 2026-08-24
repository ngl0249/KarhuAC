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
import me.liwk.karhu.event.SwingEvent;

@CheckInfo(name = "Killaura (C)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = true)
public final class KillauraC extends PacketCheck {

    private boolean swung;
    private int swungAt;
    private int swings, attacks;

    public KillauraC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if(data.getClientVersion().getProtocolVersion() <= 47 && !Karhu.getInstance().isViaRewind()) {
            if (packet instanceof AttackEvent) {
                if (!swung) {
                    fail("* NoSwing\n* sw " + data.elapsed(swungAt), getBanVL(), 300);
                }
            } else if (packet instanceof FlyingEvent) {
                swung = false;
            } else if (packet instanceof SwingEvent) {
                swung = true;
                swungAt = data.getTotalTicks();
            }
        } else {
            if (packet instanceof AttackEvent) {
                if(!data.isSkipNextSwing()) {
                    ++attacks;
                } else {
                    attacks = 0;
                    swings = 0;
                }
            } else if (packet instanceof FlyingEvent) {
                if (attacks > 1) {
                    if (swings < 1) {
                        fail("* NoSwing (1.9+/ViaRw)\n* sw " + data.elapsed(swungAt), getBanVL(), 300);
                    }
                    attacks = 0;
                    swings = 0;
                }
            } else if (packet instanceof SwingEvent) {
                ++swings;
                swungAt = data.getTotalTicks();
            }
        }
    }
}

