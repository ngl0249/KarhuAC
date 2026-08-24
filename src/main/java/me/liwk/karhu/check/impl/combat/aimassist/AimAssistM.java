package me.liwk.karhu.check.impl.combat.aimassist;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.update.MovementUpdate;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "AimAssist (M)", category = Category.COMBAT, subCategory = SubCategory.AIM, experimental = true)
public class AimAssistM extends RotationCheck {

    private final Deque<Float> pitchList = new LinkedList<>();

    public AimAssistM(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(final MovementUpdate update) {


        final CustomLocation to = update.getTo();

        if (data.deltas.deltaYaw > 2.5F && Math.abs(to.pitch) <= 80
                && !data.isCinematic() && data.getLastAttackTick() <= 3
                && data.getSensitivity() != -1) {
            pitchList.add(data.deltas.deltaPitch);
        }

        if (pitchList.size() == 200) {

            final double min = MathUtil.lowest(pitchList);
            final double max = MathUtil.highest(pitchList);

            final double difference = Math.abs(max - min);

            if (difference < data.getPitchGCD() * 1.25D) {
                fail("* Weird change\n §f* d: §b" + format(4, difference)
                                + "\n §f* e: §b" + format(4, data.getPitchGCD() * 1.5D),
                        getBanVL(), 300L);
            }

            pitchList.clear();
        }
    }
}
