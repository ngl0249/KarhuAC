package me.liwk.karhu.world.nms.wrap;

import me.liwk.karhu.data.KarhuPlayer;

//todo - test
public final class WrappedEntityPlayer extends WrappedEntity {

    private final KarhuPlayer data;

    public WrappedEntityPlayer(KarhuPlayer data) {
        super(1.8, 0.6);
        this.data = data;
    }

    public void startSneaking() {
        //stuff later
    }

    public void onStopSneaking() {
        //stuff later
    }

    public void setPosition(double x, double y, double z) {
        this.data.setLastBoundingBox(data.getBoundingBox().clone());

        final double f = this.w / 2;

        this.data.getBoundingBox().setBounds(x - f, y, z - f, x + f, y + h, z + f);
        this.data.getMcpCollision().setBounds(x - f, y, z - f, x + f, y + h, z + f);

        this.data.getMcpCollision().expand(-0.001D, -0.001D, -0.001D);

        this.data.setBoundingBoxInited(true);
    }

}
