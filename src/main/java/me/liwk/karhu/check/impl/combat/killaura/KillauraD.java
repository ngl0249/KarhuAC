package me.liwk.karhu.check.impl.combat.killaura;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;

@CheckInfo(name = "Killaura (D)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false)
public final class KillauraD extends PacketCheck {


    public KillauraD(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            if (data.isBlocking() && data.getBukkitPlayer().isBlocking() && !data.isHasDig() && data.getClientVersion().isNewerThan(ClientVersion.V_1_7_10)) {
                if(++violations > 4.0D) {
                    //fail("* Blocking while hitting", getBanVL(), 600L);
                }
            } else {
                decrease(0.35D);
            }
        }
    }
}

