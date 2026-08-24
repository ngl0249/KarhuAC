package me.liwk.karhu.check.impl.combat.autoclicker;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.SwingEvent;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckInfo(name = "AutoClicker (G)", category = Category.COMBAT, subCategory = SubCategory.AUTOCLICKER, experimental = false, credits = "§c§lCREDITS: §aMexican §7made this check.")
public final class AutoClickerG extends PacketCheck {

    public AutoClickerG(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    private final Deque<Integer> delays = new ArrayDeque<>();
    private int delay, vl;

    @Override
    public void handle(final Event packet) {
        if (packet instanceof SwingEvent) {
            boolean valid = !data.isPlacing() && !data.isHasDig() && !data.isUsingItem();
            if(data.isNewerThan8()) {
                if (delay <= 5 && valid && data.elapsedMS(((SwingEvent) packet).getTimeStamp(), data.getLastFlying()) <= 70L)
                    delays.add(delay);
            } else {
                if (delay <= 5 && valid)
                    delays.add(delay);
            }
            if (delays.size() == 27) {
                int delta = delays.stream().mapToInt(i -> i).max().orElse(0) - delays.stream().mapToInt(i -> i).min().orElse(0);

                if (delta == 1) {
                    if (++vl > 22) {
                        fail("* Impossible large-sample sequence", getBanVL(), 10000L);
                    }
                } else if (delta == 2) {
                    vl = Math.max(vl - 6, 0);
                } else {
                    vl = Math.max(vl - 12, 0);
                }
                delays.clear();
            }
            delay = 0;
        } else if (packet instanceof FlyingEvent) {
            delay++;
        }
    }
}