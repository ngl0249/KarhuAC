package me.liwk.karhu.check.impl.combat.aimassist.analysis;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.update.MovementUpdate;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

@CheckInfo(name = "Analysis (F)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = true)
public class AnalysisF extends RotationCheck {

    private final Deque<Float> pitchMatchList = new LinkedList<>();
    private final Deque<Float> yawMatchList = new LinkedList<>();

    public AnalysisF(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {

        if(data.getLastAttackTick() <= 1 && data.getLastTarget() != -696969 && data.deltas.deltaXZ > 0.1) {
            EntityData edata = data.getEntityData().get(data.getLastTarget());

            if(edata != null) {

                AxisAlignedBB entityBB = edata.getEntityBoundingBox();

                float deltaYaw = data.deltas.deltaYaw;
                float deltaPitch = data.deltas.deltaPitch;

                float[] rotationBasic = getRotations(update.from, entityBB);

                if (deltaYaw > 0F) {
                    float delta = MathUtil.getAngleDistance(rotationBasic[0], update.to.yaw);

                    yawMatchList.add(delta);
                }

                if (deltaPitch > 0F) {
                    float delta = MathUtil.getAngleDistance(rotationBasic[1], update.to.pitch);

                    pitchMatchList.add(delta);
                }
            }
        }

        if(yawMatchList.size() == 150) {
            Deque<Float> closes = yawMatchList.stream()
                    .filter(delta -> delta <= 1.5F)
                    .collect(Collectors.toCollection(LinkedList::new));
            int matches = closes.size();
            if(matches >= 110) {
                double average = MathUtil.getAverage(closes);
                fail("* Rotation analysis (common, yaw)" +
                        "\n §f* avg: §b" + average +
                        "\n §f* rate: §b" + matches, getBanVL(), 300L);
            }

            yawMatchList.clear();
        }

        if(pitchMatchList.size() == 150) {
            Deque<Float> closes = pitchMatchList.stream()
                    .filter(delta -> delta <= 2F)
                    .collect(Collectors.toCollection(LinkedList::new));
            int matches = closes.size();
            if(matches >= 110) {
                double average = MathUtil.getAverage(closes);
                fail("* Rotation analysis (common, pitch)" +
                        "\n §f* avg: §b" + average +
                        "\n §f* rate: §b" + matches, getBanVL(), 300L);
            }

            pitchMatchList.clear();
        }
    }

    private float[] getRotations(CustomLocation playerLocation, AxisAlignedBB aabb) {

        double diffX = aabb.getCenterX() - playerLocation.getX();
        double diffY = aabb.minY + 1.62D * 0.9D - (playerLocation.y + 1.62D);
        double diffZ = aabb.getCenterZ() - playerLocation.getZ();

        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
        return new float[]{
                playerLocation.yaw + MathHelper.wrapAngleTo180_float(yaw - playerLocation.yaw),
                playerLocation.pitch + MathHelper.wrapAngleTo180_float(pitch - playerLocation.pitch) + 4.0F
        };
    }

}
