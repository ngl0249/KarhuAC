package me.liwk.karhu.check.impl.packet.timer;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.event.PositionEvent;
import me.liwk.karhu.event.TickEndEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.task.Tasker;

@CheckInfo(name = "Timer (A)", category = Category.PACKET, subCategory = SubCategory.TIMER, experimental = false)
public final class TimerA extends PacketCheck {

    private long lastFlyingPacket = data.getTransactionClock();
    private long balance;

    private boolean capped, flyingBeforeTick;

    private static final long TELEPORT_OFFSET = 50000000;
    private static final long FLYING_OFFSET = 50000000;

    public TimerA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof FlyingEvent) {
            checkTimer(((FlyingEvent) packet).getNanoTime());
            flyingBeforeTick = true;
        } else if (packet instanceof PositionEvent) {
            balance -= TELEPORT_OFFSET;
        } else if (packet instanceof TickEndEvent) {
            if (!flyingBeforeTick && data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
                checkTimer(((TickEndEvent) packet).getNanoTime());
            }

            flyingBeforeTick = false;
        }
    }

    private void checkTimer(long nanoTime) {
        //Fix clock when first transaction is received
        if (data.getTransactionClock() == 0 && lastFlyingPacket == 0) {
            return;
        } else if (data.getTransactionClock() != 0 && lastFlyingPacket == 0) {
            lastFlyingPacket = data.getTransactionClock() - 250000000; //Magic value smd
        }

        long capLenght = Karhu.getInstance().getConfigManager().getTimerACapLenght() + MathUtil.toNanos(2000L);
        long now = nanoTime;
        long delay = FLYING_OFFSET - (now - lastFlyingPacket);

        long diff = Math.max(FLYING_OFFSET, (now - lastFlyingPacket));

        balance = Math.max(-capLenght, balance + delay);

        if (balance > FLYING_OFFSET + MathUtil.toNanos(5L)) { //Extra 5ms, because of precision error

            if (ready()) {
                if (++violations > 1) {
                    if (!capped) {
                        fail("* Timer\n§f* BL §b" + balance / 1000000L
                                        + "\n§f* RATE §b" + Math.min(FLYING_OFFSET / diff, 10)
                                        + "\n§f* EXISTED §b" + data.getTotalTicks(),
                                getBanVL(), 120);
                    } else kickTimer();
                }
            } else {
                disallowMove(false);
            }

            balance = 0L;
        } else {
            violations = Math.max(0, violations - 0.005);
        }

        if (balance <= -capLenght) {
            capped = true;
        }

        lastFlyingPacket = now;
    }

    private boolean ready() {
        return (data.isHasReceivedTransaction() || data.isHasReceivedKeepalive()) && data.getTotalTicks() > 100;
    }

    private void kickTimer() {
        if (!data.isTimerKicked()) {
            Tasker.run(() -> {
                data.getBukkitPlayer().kickPlayer("Timed out (T.A)");
            });

            data.setTimerKicked(true);
        }
    }
}


