package me.liwk.karhu.check.impl.movement.vehicle;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.VehicleEvent;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.entity.Boat;

@CheckInfo(name = "VehicleSpeed (A)", category = Category.MOVEMENT, subCategory = SubCategory.SPEED, experimental = true)
public final class VehicleSpeed extends PacketCheck {

    private double lastX, lastY, lastZ;

    private boolean lastGravity;

    private int ticks;

    public VehicleSpeed(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event event) {

        if(event instanceof VehicleEvent) {

            double deltaX = Math.abs(data.getVehicleX() - lastX);
            double deltaY = data.getVehicleY() - lastY;
            double deltaZ = Math.abs(data.getVehicleZ() - lastZ);

            double deltaXZ = MathUtil.hypot(deltaX, deltaZ);

            EntityData entityData = data.getEntityData().get(data.getVehicleId());

            if (entityData == null) return;

            boolean gravity = entityData.gravity;

            if (data.getVehicle() != null && gravity && lastGravity) {


                if (++ticks > 3) {

                    if (deltaXZ > 2 && data.getVehicle() instanceof Boat) {
                        fail("* Moving fast with " + data.getVehicle().getName(), 300L);
                        setLast();
                        return;
                    }
                }

                setLast();
            }

            lastGravity = gravity;
        }

        if (data.getVehicleId() == -1) ticks = 0;

    }

    private void setLast() {
        lastX = data.getVehicleX();
        lastY = data.getVehicleY();
        lastZ = data.getVehicleZ();
    }
}
