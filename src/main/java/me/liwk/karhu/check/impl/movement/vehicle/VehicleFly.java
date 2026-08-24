package me.liwk.karhu.check.impl.movement.vehicle;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.VehicleEvent;

@CheckInfo(name = "VehicleFly (A)", category = Category.MOVEMENT, subCategory = SubCategory.FLY, experimental = true)
public final class VehicleFly extends PacketCheck {

    private double lastX, lastY, lastZ;

    private boolean lastGravity;

    private double violationsZero;

    private int ticks;

    public VehicleFly(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event event) {

        if(event instanceof VehicleEvent) {

            double deltaX = Math.abs(data.getVehicleX() - lastX);
            double deltaY = data.getVehicleY() - lastY;
            double deltaZ = Math.abs(data.getVehicleZ() - lastZ);


            if (data.getVehicle() != null) {

                EntityData entityData = data.getEntityData().get(data.getVehicleId());

                if (entityData == null) return;

                boolean gravity = entityData.gravity;

                if (gravity && lastGravity) {

                    if (++ticks > 3) {

                        if (deltaY > 1.5) {
                            fail("* Moving upwards with " + data.getVehicle().getName(), 300L);
                        } else if (deltaY > 0.5 && EntityTypes.isTypeInstanceOf(data.getVehicle(), EntityTypes.BOAT)) {
                            fail("* Moving upwards with boat (2)", 300L);
                        }

                        /*if (deltaY == 0 && (deltaX > 0 || deltaZ > 0) && data.getVehicle() instanceof Boat && !ground && !lastGround) {
                            if (++violationsZero > 2) {
                                fail("* Moving 0 vertical with boat", 300L);
                                setLast(ground);
                                return;
                            }
                        } else {
                            violationsZero = Math.max(violationsZero - 0.1, 0);
                        }*/
                    }

                }

                lastGravity = gravity;
                setLast();
            }
        }

        if (data.getVehicleId() == -1) ticks = 0;

    }

    private void setLast() {
        lastX = data.getVehicleX();
        lastY = data.getVehicleY();
        lastZ = data.getVehicleZ();
    }
}
