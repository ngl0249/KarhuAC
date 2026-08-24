package me.liwk.karhu.check.impl.world.ground;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckInfo(name = "Ground (C)", category = Category.WORLD, subCategory = SubCategory.NOFALL, experimental = false)
public final class GroundC extends PacketCheck {

    public GroundC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {

        if (packet instanceof FlyingEvent) {

            double groundDiff = Math.abs(data.getLocation().y) % 0.015625;

            if (this.data.elapsed(this.data.getLastFlyTick()) <= 20 || data.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            if (!this.data.isOnClimbable()
                    && !data.isOnSlab()
                    && !data.isInsideBlock()
                    && !data.isOnStairs()
                    && !data.isOnBoat()
                    && !data.isInUnloadedChunk()
                    && data.elapsed(data.getLastPossibleInUnloadedChunk()) > 1
                    && !data.isWasInUnloadedChunk()
                    && !data.isPossiblyTeleporting()
                    && !data.isOnSlime()
                    && !data.isGroundNearBox()
                    && data.getPositionPackets() > 60
                    //&& !data.isOnGhostBlock()
                    && !data.isRiding()
                    && !data.isWasOnSlime()
                    && data.elapsed(data.getLastInLiquid()) > 2
                    && !data.isInWeb()
                    && data.getLevitationLevel() == 0
                    && data.elapsed(data.getPredictionTicks()) >= 1
                    && !data.isWasInWeb()
                    && !data.isSpectating()
                    && data.elapsed(data.getPlaceTicks()) > Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L) + 5)) {

                double MAX = data.getClientAirTicks() < data.getAirTicks() / 3 ? 1 : 2;

                boolean groundServer = groundDiff == 0;
                
                boolean clientCollide = ((FlyingEvent) packet).isOnGround();

                if (clientCollide != groundServer && data.getAirTicks() > 2) {
                    if (this.violations++ > MAX) {
                        this.fail("* Spoofed ground status" +
                                "\n §f* ST/CT: §b" + this.data.getAirTicks() + " | " + this.data.getClientAirTicks() +
                                "\n §f* MG/CG: §b" + groundServer + " | " + clientCollide +
                                "\n §f* difference: §b" + groundDiff, getBanVL(), 50);
                    }
                } else {
                    this.violations = Math.max(violations - 0.045D, 0);
                }

            }

        }

    }

}

