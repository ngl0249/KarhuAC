package me.liwk.karhu.check.impl.packet.timer;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.PositionEvent;
import me.liwk.karhu.util.MathUtil;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Timer (C)", category = Category.PACKET, subCategory = SubCategory.TIMER, experimental = true)
public final class TimerC extends PacketCheck {

    private final Deque<Long> packets = new LinkedList<>();

    private Long lastFlyingPacket = null;
    private long lastFlag;

    public TimerC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
        violations = -1;
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof FlyingEvent && !data.recentlyTeleported(5)) {

            long now = ((FlyingEvent) packet).getCurrentTimeMillis();

            if (lastFlyingPacket != null) {

                final long timeDiff = now - lastFlyingPacket;

                packets.add(timeDiff);

                if (packets.size() >= 50) {
                    double average = MathUtil.getAverage(packets);
                    double timer = 50D / average;

                    double speed = !data.isNewerThan8() ? 1.25 : 1.10;

                    if (timer > speed && data.getTotalTicks() > 200) {
                        if (++violations > 3) {
                            fail("* Timer"
                                            + "\n§f* TS §b" + format(2, timer)
                                            + "\n§f* BADS §b" + packets.stream().filter(l -> (l < 50L)).count() + "/50",
                                    getBanVL(), 300);
                        }
                    } else {
                        if(now - lastFlag > 12500L) {
                            violations = Math.max(violations - 1.25, -2);
                        } else {
                            violations = Math.max(violations - 0.65, -1);
                        }
                    }

                    packets.clear();
                }
            }

            lastFlyingPacket = now;
        } else if (packet instanceof PositionEvent) {
            packets.add(150L);
        } else if (packet instanceof BlockPlaceEvent && data.getClientVersion().getProtocolVersion() > 754) {
            packets.add(100L);
        }

    }
}

