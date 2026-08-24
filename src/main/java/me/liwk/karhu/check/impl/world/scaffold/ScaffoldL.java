package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.player.PlayerUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@CheckInfo(name = "Scaffold (L)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldL extends PacketCheck {

    private int movements, jumps, noJump, sameYStreak;
    private double lastY;
    private boolean sameY;
    private boolean placed;

    public ScaffoldL(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        //if (data.isNewerThan16()) return;

        if (packet instanceof BlockPlaceEvent) {

            Vector pos = ((BlockPlaceEvent) packet).getBlockPos();
            int face = ((BlockPlaceEvent) packet).getFace();

            if ((pos.getX() != -1 && (pos.getY() != 255 && pos.getY() != -1) && pos.getZ() != -1)) {

                if (face != 1) {

                    if (movements <= 10) {
                        ItemStack item = ((BlockPlaceEvent) packet).getItemStack();
                        if (item != null && item.getType().isBlock()) {

                            Vector location = data.getLocation().toVector();

                            if (location.distance(pos) <= 2
                                    && data.isNotGroundBridging()
                                    && location.getY() > pos.getY()) {

                                sameY = (lastY == pos.getY());

                                lastY = pos.getY();

                                placed = true;
                            }

                        }
                    }
                }

            }
            movements = 0;
        } else if (packet instanceof FlyingEvent) {
            ++movements;

            if (placed) {
                boolean eligible = data.deltas.deltaXZ > PlayerUtil.getBaseSpeedAttribute(data, 1.8f) && data.elapsed(data.getLastVelocityTaken()) > 5;
                if (eligible) {
                    if (!sameY) sameYStreak = 0;
                    if ((data.isJumped() || data.isJumpedLastTick()) || (!data.isOnGroundPacket() && sameY)) {
                        if (sameY) {
                            ++sameYStreak;
                        }
                        ++jumps;
                    } else {
                        ++noJump;
                    }
                }
            }

            if (jumps > 10 && noJump >= 0) {
                if (jumps > noJump && sameYStreak > 2) {
                    String info = String.format("J %s, NJ %s SY %s", jumps, noJump, sameYStreak);

                    fail("* Scaffold pattern" +
                            "\n" + info, getBanVL(), 250L);
                }
                sameYStreak = jumps = noJump = 0;
            }
            placed = false;
        }
    }
}
