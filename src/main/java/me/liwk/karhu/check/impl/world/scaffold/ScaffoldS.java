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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@CheckInfo(name = "Scaffold (S)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true)
public final class ScaffoldS extends PacketCheck {


    public ScaffoldS(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        if (packet instanceof BlockPlaceEvent) {
            BlockPlaceEvent place = ((BlockPlaceEvent) packet);

            float sneakAmount1_8 = data.isSneaking() ? 1.54F : 1.62F;
            float sneakAmount1_13 = data.isSneaking() ? 1.27F : 1.62F;

            Vector blockPos = place.getOrigin();

            ItemStack stack = place.getItemStack() == null ? new ItemStack(Material.AIR) : place.getItemStack();

            if (place.isUsableItem() || !stack.getType().isSolid() || !stack.getType().isBlock()) {
                decrease(0.15D);
                return;
            }

            if (data.isOnClimbable()
                    || data.isNearClimbable()
                    || data.isInsideTrapdoor()
                    || data.isCollidedHorizontally()
                    || data.isRiding()
                    || data.isAtButton()
                    || data.isOnFence()
                    || data.isWasOnFence()
                    || data.isOnDoor()
                    || data.isNearDoor()
                    || data.isWasOnDoor()
                    || data.elapsed(data.getYawFucked()) <= 1
                    || data.isOnStairs()
                    || data.isOnSlab()) {
                return;
            }


            if (data.getLocation().y - blockPos.getY() > 0.42F) {
                Vector eyeLocation = new Vector(data.getLocation().x,
                        data.getLocation().y + (!data.isNewerThan12()
                                ? sneakAmount1_8 : sneakAmount1_13),
                        data.getLocation().z);

                boolean correctInteract = MathUtil.isReallyPlacingBlock(blockPos, eyeLocation, place.getDirection());

                if (!correctInteract) {

                    int diffX = (int) (blockPos.getX() - eyeLocation.getBlockX()),
                            diffY = (int) (blockPos.getY() - eyeLocation.getBlockY()),
                            diffZ = (int) (blockPos.getZ() - eyeLocation.getBlockZ());

                    Vector dirVec = MathUtil.getDirection2(data.getLocation().yaw, data.getLocation().pitch);
                    Vector dirVecO = MathUtil.getDirection2(data.getLastLocation().yaw, data.getLocation().pitch);

                    double eyeDistanceX = MathUtil.getBlockDistance(eyeLocation.getX(), eyeLocation.getBlockX(), dirVec.getX(), diffX),
                            eyeDistanceY = MathUtil.getBlockDistance(eyeLocation.getY(), eyeLocation.getBlockY(), dirVec.getY(), diffY),
                            eyeDistanceZ = MathUtil.getBlockDistance(eyeLocation.getZ(), eyeLocation.getBlockZ(), dirVec.getZ(), diffZ);

                    double eyeDistanceXO = MathUtil.getBlockDistance(eyeLocation.getX(), eyeLocation.getBlockX(), dirVecO.getX(), diffX),
                            eyeDistanceYO = MathUtil.getBlockDistance(eyeLocation.getY(), eyeLocation.getBlockY(), dirVecO.getY(), diffY),
                            eyeDistanceZO = MathUtil.getBlockDistance(eyeLocation.getZ(), eyeLocation.getBlockZ(), dirVecO.getZ(), diffZ);


                    double distanceFromEye = Math.max(0, Math.max(eyeDistanceX, Math.max(eyeDistanceY, eyeDistanceZ)));
                    double distanceFromEyeO = Math.max(0, Math.max(eyeDistanceXO, Math.max(eyeDistanceYO, eyeDistanceZO)));

                    double lowestDistance = Math.min(distanceFromEyeO, distanceFromEye);

                    if (lowestDistance > 1 + data.offsetMove()) {
                        if (data.isNotGroundBridging() && increase(1) > 1) {
                            if (lowestDistance < 1.5) {
                                fail(String.format("* Invalid interact d: %.2f", lowestDistance), 300L);
                            } else {
                                fail("* Invalid interact", 300L);
                            }
                        }
                    }
                }
            } else {
                decrease(0.125);
            }
        }
    }
}
