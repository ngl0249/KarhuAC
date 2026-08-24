package me.liwk.karhu.check.impl.world.scaffold;

import com.github.retrooper.packetevents.util.Vector3i;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import org.bukkit.util.Vector;

@CheckInfo(name = "Scaffold (M)", category = Category.WORLD, subCategory = SubCategory.SCAFFOLD, experimental = true, credits = "§c§lCREDITS: §aIslandscout §7for the base idea.")
public final class ScaffoldM extends PacketCheck {

    private double buffer;

    public ScaffoldM(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {

        //if(data.isNewerThan16()) return;

        if (packet instanceof BlockPlaceEvent) {
            final Vector pos = ((BlockPlaceEvent)packet).getOrigin();
            final Vector targetPos = ((BlockPlaceEvent)packet).getBlockPos();
            if (pos.getX() != -1 && (pos.getY() != 255 || pos.getY() != -1) && pos.getZ() != -1) {
                BoundingBox hitbox = new BoundingBox(data,
                        targetPos.getX(),
                        targetPos.getY(),
                        targetPos.getZ(),
                        targetPos.getX() + 1,
                        targetPos.getY() + 1,
                        targetPos.getZ() + 1);

                final int face = ((BlockPlaceEvent) packet).getFace();
                final Vector3i facePosInt = ((BlockPlaceEvent) packet).getBlockFacePosition();
                final Vector facePos =  new Vector(facePosInt.getX(), facePosInt.getY(), facePosInt.getZ());

                float sneakAmount1_8 = data.isSneaking() ? 1.54F : 1.62F;
                float sneakAmount1_13 = data.isSneaking() ? 1.27F : 1.62F;

                Vector eyeLocation = new Vector(data.getLocation().x,
                        data.getLocation().y + (!data.isNewerThan12()
                                ? sneakAmount1_8 : sneakAmount1_13),
                        data.getLocation().z);

                Vector eyeLocationFixed = new Vector(data.getLastLocation().x,
                        data.getLastLocation().y + (!data.isNewerThan12()
                                ? sneakAmount1_8 : sneakAmount1_13),
                        data.getLastLocation().z);

                if(!data.isInsideBlock() && !data.isInWeb() && !data.isOnWeb()
                        && !data.isCollidedHorizontally() && !data.isRiding() && !data.isSpectating() && !data.isPossiblyTeleporting()) {
                    double dot1 = facePos.dot(MathUtil.getDirection(data.getLocation().yaw, data.getLocation().pitch));
                    double dot2 = facePos.dot(MathUtil.getDirection(data.getLastLocation().yaw, data.getLocation().pitch));
                    if(dot1 >= 0
                            && dot2 >= 0
                            && !hitbox.hasPoint(eyeLocation)
                            && !hitbox.hasPoint(eyeLocationFixed)
                            && data.isNotGroundBridging()) {
                        if(++buffer > 5) {
                            fail("* Impossible block placement" + "\n §f* D: §b" + dot1, getBanVL(), 120);
                        }
                    } else {
                        buffer = Math.max(buffer - 0.5, 0);
                    }
                }
            }
        }
    }
}
