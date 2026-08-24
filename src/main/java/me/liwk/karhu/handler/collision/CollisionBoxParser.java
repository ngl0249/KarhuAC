package me.liwk.karhu.handler.collision;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import me.liwk.karhu.handler.collision.enums.Boxes;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class CollisionBoxParser {

    public static AxisAlignedBB from(Entity e) {

        AxisAlignedBB b;

        final Location l = e.getLocation();

        switch (e.getType()) {

            case BOAT: {
                b = fromBoxEnum(l, Boxes.BOAT);
                break;
            }

            case PLAYER: {
                b = fromBoxEnum(l, Boxes.PLAYER);
                break;
            }

            //todo - this is until i add all the boundingboxes for entites
            default: {
                b = MathUtil.getEntityBoundingBox(l);
                break;
            }

        }

        return b;
    }

    public static Boxes from(EntityType e) {

        if (EntityTypes.isTypeInstanceOf(e, EntityTypes.BOAT)) {
            return Boxes.BOAT;
        } else if (EntityTypes.isTypeInstanceOf(e, EntityTypes.SHULKER)) {
            return Boxes.SHULKER;
        } else if (e == EntityTypes.HAPPY_GHAST) {
            return Boxes.HAPPY_GHAST;
        } else {
            return Boxes.PLAYER;
        }
    }

    private static AxisAlignedBB fromBoxEnum(Location l, Boxes e) {
        return new AxisAlignedBB(l.getX() - e.getWidth(), l.getY(), l.getZ() - e.getWidth(), l.getX() + e.getWidth(), l.getY() + e.getHeight(), l.getZ() + e.getWidth());
    }

}
