package me.liwk.karhu.check.impl.movement.water;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "Jesus (B)", category = Category.MOVEMENT, subCategory = SubCategory.JESUS, experimental = true)
public final class JesusB extends PositionCheck {

    private int zeroTicks;

    public JesusB(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {
        if ((data.isOnLiquid() || data.isAboveButNotInWater())
                && data.elapsed(data.getUnderPlaceTicks()) > data.getPingInTicks() + 5
                && data.elapsed(data.getLastFlyTick()) > 30) {

            double minMove = 1E-3D;

            if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_13)) {
                if (data.getBukkitPlayer().isSwimming()) {
                    zeroTicks = 0;
                    return;
                }
            } else {
                if (data.getClientVersion().getProtocolVersion() > 340) {
                    zeroTicks = 0;
                    return;
                }
            }

            if (data.getAirTicks() > 4
                    && !update.isGround()
                    && !data.isUnderBlock() && !data.isWasUnderBlock()
                    && !data.isInWeb() && !data.isOnWeb()
                    && data.elapsed(data.getLastVelocityTaken()) > 2
                    && !data.isPossiblyTeleporting()) {
                if (Math.abs(data.deltas.motionY) < minMove && Math.abs(data.deltas.motionY) > 0.0D) {
                    if (++violations > 3) {
                        fail("* Illegal y-axis movement in liquid"
                                + "\n§f* Inside §b" + data.isOnLiquid()
                                + "\n§f* Above §b" + data.isAboveButNotInWater()
                                + "\n§f* motionY §b" + Math.abs(data.deltas.motionY), getBanVL(), 200);
                    }
                    zeroTicks = 0;
                } else if(Math.abs(data.deltas.motionY) == 0.0D) {
                    if(++zeroTicks > 3) {
                        fail("* Illegal y-axis movement in liquid (zeros)"
                                + "\n§f* Inside §b" + data.isOnLiquid()
                                + "\n§f* Above §b" + data.isAboveButNotInWater()
                                + "\n§f* motionY §b" + Math.abs(data.deltas.motionY), getBanVL(), 200);
                    }
                } else {
                    violations = Math.max(violations - 0.05, 0);
                    zeroTicks = 0;
                }
            }
        }
    }
}
