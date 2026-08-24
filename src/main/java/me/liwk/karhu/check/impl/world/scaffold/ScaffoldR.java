package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@CheckInfo(name = "Scaffold (R)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = false, credits = "§c§lCREDITS: §aIslandscout §7for the base idea.")
public final class ScaffoldR extends PacketCheck {


    public ScaffoldR(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        //if(data.isNewerThan16()) return;

        if (packet instanceof BlockPlaceEvent) {
            BlockPlaceEvent place = ((BlockPlaceEvent) packet);

            float sneakAmount1_8 = data.isSneaking() ? 1.54F : 1.62F;
            float sneakAmount1_13 = data.isSneaking() ? 1.27F : 1.62F;

            Location blockPos = place.getTargetedBlockLocation();

            ItemStack stack = place.getItemStack() == null ? new ItemStack(Material.AIR) : place.getItemStack();

            if(place.isUsableItem() || !stack.getType().isSolid() || !stack.getType().isBlock()) {
                decrease(0.15D);
                return;
            }

            Vector eyeLocation = new Vector(data.getLocation().x,
                    data.getLocation().y + (!data.isNewerThan12()
                            ? sneakAmount1_8 : sneakAmount1_13),
                    data.getLocation().z);

            Vector dir = MathUtil.getDirection(data.getLocation().getYaw(), data.getLocation().getPitch());
            Vector extraDir = MathUtil.getDirection(data.getLastLastLocation().getYaw(),
                    data.getLastLastLocation().getPitch());
            Vector extraDir2 = MathUtil.getDirection(data.getLastLocation().getYaw(),
                    data.getLastLocation().getPitch());


            AxisAlignedBB targetAABB = new AxisAlignedBB(blockPos.toVector(), blockPos.toVector(), true);
            targetAABB = targetAABB.addCoord(1F, 1F, 1F).expand(0.75, 0.75, 0.75);

            boolean betweenRays = targetAABB.betweenRays(eyeLocation, dir, dir);

            boolean betweenFirst = targetAABB.betweenRays(eyeLocation, dir, extraDir);
            boolean betweenSecond = targetAABB.betweenRays(eyeLocation, dir, extraDir2);

            if(!betweenRays && !betweenFirst && !betweenSecond
                    && !data.isRiding() && !data.isAtSign() && !data.isNearClimbable()
                    && data.deltas.deltaYaw < 5
                    && data.deltas.deltaPitch < 5) {
                if (data.isNotGroundBridging()) {
                    if (++violations > 5) {
                        fail("* Invalid place (expand scaffold?)" +
                                "\n* face: " + place.getFace() +
                                "\n* pos: " + place.getOrigin(), getBanVL(), 300L);
                    }
                } else {
                    decrease(0.5D);
                }
            }
        }
    }
}
