package me.liwk.karhu.check.impl.packet.badpackets;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.InteractEvent;

@CheckInfo(name = "BadPackets (O)", category = Category.PACKET, subCategory = SubCategory.BADPACKETS, experimental = false)
public final class BadPacketsO extends PacketCheck {

    public BadPacketsO(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final Event packet) {
        if (packet instanceof InteractEvent) {
            InteractEvent interactEvent = (InteractEvent) packet;
            if (interactEvent.isPlayer()) {
                Vector3f vector3d = interactEvent.getVec3D();
                if (vector3d != null && interactEvent.isAt()) {
                    final double x = Math.abs(vector3d.x), y = vector3d.y, z = Math.abs(vector3d.z);

                    EntityData entityData = data.getEntityData().get(((InteractEvent) packet).getEntityId());

                    if (entityData != null) {

                        final EntityType e = entityData.getType();

                        if (!EntityTypes.isTypeInstanceOf(e, EntityTypes.PLAYER)) return;

                        float scale = entityData.getScale();

                        final double expandX = (x - 0.4005 * scale), expandY = (y - 1.905 * scale), expandZ = (z - 0.4005 * scale);

                        String entityName = e.getName().toString();

                        if (expandX > 0 || expandY > 0 || y < -0.105 || expandZ > 0) {
                            int expand = (int) Math.round((expandX > 0 ? expandX : 0
                                    + expandY > 0 ? expandY : 0
                                    + expandZ > 0 ? expandZ : 0) * 100);
                            fail("* Wrong hitbox in packet"
                                    + "\n§f* expand §b" + expand + "%"
                                    + String.format("\n§f* x§b %.3f§f, y§b %.3f§f, z§b %.3f", expandX, expandY, expandZ)
                                    + "\n§f* type §b" + entityName, 420L);
                        }
                    }
                }
            }
        }
    }
}