package me.liwk.karhu.util.pending;

import lombok.*;

@Getter
@Setter
public class PositionPending implements Cloneable {

    public int transactionId;
    public int[] entities;
    public int entityId;
    public double x, y, z;
    public int type;
    public boolean confirm;

    public PositionPending(int transactionId, int[] entities, double x, double y, double z, int type, boolean confirm) {
        this.transactionId = transactionId;
        this.entities = entities;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.confirm = confirm;
    }

    public PositionPending(int transactionId, int entityId, double x, double y, double z, int type, boolean confirm) {
        this.transactionId = transactionId;
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.confirm = confirm;
    }

    @SneakyThrows
    public PositionPending clone() {
        return (PositionPending) super.clone();
    }

}
