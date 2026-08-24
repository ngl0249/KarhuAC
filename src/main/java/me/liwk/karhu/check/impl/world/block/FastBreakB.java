package me.liwk.karhu.check.impl.world.block;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.DigEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.RespawnEvent;
import me.liwk.karhu.event.SwingEvent;

@CheckInfo(name = "FastBreak (B)", category = Category.WORLD, subCategory = SubCategory.BLOCK, experimental = false)
public final class FastBreakB extends PacketCheck {

    private int blockHitDelay;

    public FastBreakB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {

        if(packet instanceof SwingEvent) {
            if (this.blockHitDelay > 0) --blockHitDelay;

        } else if(packet instanceof DigEvent) {

            DiggingAction digType = ((DigEvent) packet).getDigType();

            switch (digType) {
                case START_DIGGING:
                    this.blockHitDelay = 5;
                    break;

                case FINISHED_DIGGING:
                    //if(this.blockHitDelay > 0) fail("* Fastbreak (cooldown removed)\n\n* delay: " + this.blockHitDelay, getBanVL(), 600L);
                    this.blockHitDelay = 5; //4 is lenient, not going to investigate why it breaks randomly with 5
                    break;

                default: break;

            }


        } else if(packet instanceof RespawnEvent) {
            //Respawns could fuckup shit, because tickloop stops.
            this.blockHitDelay = 0;
        }
    }
}