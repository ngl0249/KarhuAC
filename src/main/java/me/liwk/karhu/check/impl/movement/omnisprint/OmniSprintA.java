package me.liwk.karhu.check.impl.movement.omnisprint;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(name = "OmniSprint (A)", category = Category.MOVEMENT, subCategory = SubCategory.SPEED, experimental = true)
public final class OmniSprintA extends PositionCheck {


    public OmniSprintA(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    public void handle(MovementUpdate e) {

    }
}
