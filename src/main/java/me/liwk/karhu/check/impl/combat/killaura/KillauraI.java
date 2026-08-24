package me.liwk.karhu.check.impl.combat.killaura;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;

@CheckInfo(name = "Killaura (I)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false)
public final class KillauraI extends PacketCheck {

    private long sentDig;

    public KillauraI(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof AttackEvent || packet instanceof InteractEvent) {
            long now;

            if(packet instanceof AttackEvent) {
                now = ((AttackEvent) packet).getNow();
            } else {
                now = ((InteractEvent) packet).getNow();
            }

            long delay = (long) ((now - sentDig) / 1E6);
            if (delay < 10L && data.elapsed(data.getLastPacketDrop()) > 5 && !this.getKarhu().isServerLagging(now) && this.getKarhu().getTPS() >= 19.95) {
                if(++violations > 5) {
                    fail("* Illegal block order", getBanVL(), 60);
                }
            } else {
                violations = Math.max(violations - 0.35, 0);
            }
        } else if (packet instanceof DigEvent) {
            DiggingAction type = ((DigEvent) packet).getDigType();
            if(type != DiggingAction.DROP_ITEM_STACK && type != DiggingAction.DROP_ITEM) {
                sentDig = ((DigEvent) packet).getNow();
            }
        }
    }
}

