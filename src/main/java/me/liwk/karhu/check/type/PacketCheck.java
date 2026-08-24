package me.liwk.karhu.check.type;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;

public abstract class PacketCheck extends Check<Event> {

    public PacketCheck(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    public boolean checkClick() {
        return !data.isPlacing() && !data.isHasDig() && !data.isUsingItem();
    }

    public abstract void handle(final Event packet);

}
