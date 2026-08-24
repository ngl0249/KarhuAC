package me.liwk.karhu.check.impl.packet.badpackets;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;

@CheckInfo(name = "BadPackets (N)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = true)
public final class BadPacketsN extends PacketCheck {

    private int lastMove;

    public BadPacketsN(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        if (packet instanceof FlyingEvent) {

            final boolean collidedHorizontally = this.data.elapsed(data.getLastCollided()) < 10 &&
                    data.elapsed(data.getLastCollidedV()) > 10;

            if (!collidedHorizontally && ((FlyingEvent) packet).hasMoved()) {

                if (lastMove++ >= 3) {

                    final double moveDist = data.getLocation().toVector().distanceSquared(data.getLastLocation().toVector());
                    final boolean invalid = moveDist <= 1E-12 && moveDist != 0 && !data.isPossiblyTeleporting();

                    if (invalid && !data.isRiding() && !data.recentlyTeleported(5)) {

                        this.violations++;

                        if (this.violations >= 2.5D) {
                            this.fail("Invalid movement packet state", getBanVL(), 300L);
                        }

                    } else {
                        this.violations = Math.max(this.violations - 0.05, 0);
                    }

                }

            } else {
                lastMove = 0;
            }

        }

    }

}