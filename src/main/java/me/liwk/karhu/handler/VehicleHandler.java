package me.liwk.karhu.handler;

import lombok.RequiredArgsConstructor;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.interfaces.IVehicleHandler;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

@RequiredArgsConstructor
public final class VehicleHandler implements IVehicleHandler {

    private final KarhuPlayer data;
    private int lastDismount;

    /*
     * You can ride any "vehicle", so no entity checks are needed but distance (i hope),
     * one day il check if you can ACTUALLY ride the entity, for now this is sufficient.
     */
    public void handle(Entity e) {

        if(data.getTotalTicks() < 20) {
            return;
        }

        if (e == this.data.getBukkitPlayer()) {
            if(Karhu.getInstance().getConfigManager().isVehicleHandler()) {
                MiscellaneousAlertPoster.postMisc(Karhu.getInstance().getConfigManager().getConfig().getString("VehicleHandlerMessage").replaceAll("%player%", this.data.getName()), data, "Vehicle");
                this.forceDismount();
            }
        } else {
            if (e instanceof Vehicle) {

                final double dist = e.getLocation().distanceSquared(data.getLocation().toLocation(data.getWorld()));
                if (dist <= 20.0D) {
                    data.setRiding(true);
                }
            }
        }
    }

    public void handleMove() {
        if(data.getTotalTicks() - lastDismount > 1){
            data.setExitingVehicle(false);
        }

        /*if(data.getTeleportManager().teleportTicks == 0) {
            data.setExitingVehicle(true);
            data.setRidding(false);

            lastDismount = data.getTotalTicks();

            if(data.getBukkitPlayer().getVehicle() != null
                    && data.elapsed(data.getLastMount()) > 2 + data.getPingInTicks()) {
                //data.getBukkitPlayer().leaveVehicle();
            }
        }*/
    }

    private void forceDismount() {
        if(Karhu.getInstance().getConfigManager().isVehicleHandler()) {
            if(data.getBukkitPlayer().getVehicle() != null && data.getBukkitPlayer().isInsideVehicle()) {
                data.getBukkitPlayer().leaveVehicle();
            }
        }
    }
}
