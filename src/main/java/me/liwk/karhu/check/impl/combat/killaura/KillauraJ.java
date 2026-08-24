package me.liwk.karhu.check.impl.combat.killaura;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;

@CheckInfo(name = "Killaura (J)", category = Category.COMBAT, subCategory = SubCategory.KILLAURA, experimental = false, credits = "§c§lCREDITS: §aMexify §7made this check.")
public final class KillauraJ extends PacketCheck {

    private boolean sent;

    public KillauraJ(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (!data.isNewerThan8()) {
            if (packet instanceof AttackEvent || packet instanceof InteractEvent) {
                if (this.sent && !data.isPossiblyTeleporting()) {
                    fail("* Illegal block order", getBanVL(), 90);
                }
            } else if (packet instanceof DigEvent) {
                DiggingAction type = ((DigEvent) packet).getDigType();
                if (type == DiggingAction.START_DIGGING || type == DiggingAction.CANCELLED_DIGGING || type == DiggingAction.RELEASE_USE_ITEM) {
                    sent = true;
                }
            } else if (packet instanceof FlyingEvent) {
                this.sent = false;
            }
        }
    }
}
