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
import me.liwk.karhu.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Queue;

@CheckInfo(name = "Scaffold (P)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = false)
public final class ScaffoldP extends PacketCheck {

    private final Queue<Integer> delays = new LinkedList<>();

    public int placed, movements;

    public ScaffoldP(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof BlockPlaceEvent) {

            if (data.isNewerThan16()) return;

            BlockPlaceEvent place = ((BlockPlaceEvent) packet);

            int face = place.getFace();

            Location blockPos = place.get420Johannes();

            ItemStack stack = place.getItemStack() == null ? new ItemStack(Material.AIR) : place.getItemStack();

            boolean validPlace = Math.abs(blockPos.getX() - data.getLocation().x) <= 1.5
                    && Math.abs(blockPos.getY() - data.getLocation().y) <= 2.5
                    && Math.abs(blockPos.getZ() - data.getLocation().z) <= 1.5;

            boolean canProcess = !data.isCollidedHorizontally()
                    && !place.isUsableItem() && data.elapsed(data.getLastFlyTick()) > 10
                    && stack.getType().isSolid() && stack.getType().isBlock();

            double offsetH = data.deltas.deltaXZ, lastOffsetH = data.deltas.lastDXZ;

            if (movements < 10 && delays.add(movements)) {
                if (delays.size() >= 50 && placed <= 2) {
                    //Clear it to prevent crazy sorting in getAverage
                    delays.clear();
                }
            }

            if (face >= 2 && face <= 5 && validPlace) {
                if (offsetH > (data.elapsed(data.getLastSneakTick()) <= 10 ? 0.16 : 0.12) && canProcess) {

                    Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(blockPos);

                    if (block != null) {

                        boolean additionable = block.getType() == stack.getType() && data.isNotGroundBridging();

                        if (additionable && ++placed >= 10) {

                            double avg = MathUtil.getAverage(delays);
                            double cps = 20.0 / avg;

                            fail("* Impossible bridging" +
                                    "\n §f* balance: §b" + placed +
                                    "\n §f* oH | lOH: §b" + format(3, offsetH) + " | " + format(3, lastOffsetH) +
                                    "\n §f* cps: §b" + format(3, cps) +
                                    "\n §f* avg: §b" + format(3, avg), getBanVL(), 120);

                            delays.clear();

                            placed = 2;
                        }
                    }
                }

            } else {
                placed = Math.max(placed - 2, -5);
            }

            movements = 0;
        } else if (packet instanceof FlyingEvent) {
            ++movements;
        }
    }
}
