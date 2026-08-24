package me.liwk.karhu.check.impl.packet.timer;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.Bukkit;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Timer (B)", category = Category.PACKET, subCategory = SubCategory.TIMER, experimental = false)
public final class TimerB extends PacketCheck {

    private final Deque<Long> packets = new LinkedList<>();

    private Long lastFlyingPacket = null;

    public TimerB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof FlyingEvent) {

            long now = ((FlyingEvent) packet).getCurrentTimeMillis();

            if(Karhu.getInstance().hasRecentlyDropped(2250L)) {
                packets.clear();
            }

            if (lastFlyingPacket != null && !Karhu.getInstance().hasRecentlyDropped(5000L)) {

                final long timeDiff = now - lastFlyingPacket;

                packets.add(timeDiff);

                if (packets.size() >= 50) {
                    double average = MathUtil.getAverage(packets);
                    double timer = 50D / average;

                    double speed = !data.isNewerThan8() ? 0.75 : 0.02;
                    final double deviation = MathUtil.getStandardDeviation(packets);

                    double addition = deviation < 8 ? 2.5 : 1.25;

                    addition += timer <= speed / 3 ? 1.25 : 0;

                    if (timer <= speed && deviation < 150) {
                        if ((violations += addition) > 3.5) {
                            fail("* Timer\n§f* TS §b" + format(2, timer) + "\n§f* DEV §b" + deviation, getBanVL(), 400);
                        }
                    } else {
                        violations = Math.max(violations - 0.65, 0);
                    }

                    packets.clear();
                }
            }

            lastFlyingPacket = now;
        }
    }
}
