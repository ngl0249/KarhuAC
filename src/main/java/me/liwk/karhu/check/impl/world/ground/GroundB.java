package me.liwk.karhu.check.impl.world.ground;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;

@CheckInfo(name = "Ground (B)", category = Category.WORLD, subCategory = SubCategory.NOFALL, experimental = false)
public final class GroundB extends PacketCheck {

    public GroundB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {

        if (packet instanceof FlyingEvent) {

            if (this.data.elapsed(this.data.getLastFlyTick()) <= 20 || data.getGameMode() == GameMode.CREATIVE && !data.isPossiblyTeleporting()) {
                return;
            }

            if (!this.data.isPossiblyTeleporting()) {

                if (this.data.getAirTicks() > 10
                        && !this.data.isOnClimbable()
                        && !data.isInsideBlock()
                        && !data.isInWeb()
                        && !data.isGroundNearBox()
                        && !data.isSpectating()
                        && !this.data.isOnGhostBlock()
                        && !data.isInUnloadedChunk()
                        && (data.isHasReceivedTransaction() || data.getTotalTicks() > 120)
                        && !data.isWasInUnloadedChunk()
                        && !data.isWasInWeb()
                        && data.elapsed(data.getPlaceTicks()) > Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L) + 2)) {

                    double MAX = data.elapsed(data.getPlaceTicks()) <= Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L) + 5) ? 5 : 4.25;

                    MAX += data.elapsed(data.getLastPacketDrop()) < 5 ? 2 : 1.25;

                    if (((FlyingEvent) packet).isOnGround()) {
                        if (++this.violations > MAX) {
                            this.fail("* Spoofed ground status" +
                                    "\n* CT: §b" + this.data.getClientAirTicks() +
                                    "\n* ST: §b" + this.data.getAirTicks() +
                                    "\n* UNLOADED: §b" + this.data.elapsed(data.getLastInUnloadedChunk()) +
                                    "\n* MOVE: §b" + this.data.getMoveTicks() +
                                    "\n* NOMOVE: §b" + this.data.getNoMoveTicks(),
                                    getBanVL(), 40);
                        }
                    } else {
                        this.violations = Math.max(violations - 0.1D, 0);
                    }

                }

            }

        }

    }

}
