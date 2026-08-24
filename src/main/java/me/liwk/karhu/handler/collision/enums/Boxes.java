package me.liwk.karhu.handler.collision.enums;

import lombok.Getter;

public enum Boxes {

    BOAT(0.6F, 1.5F),

    SHULKER(1.20609F, 1F),
    HAPPY_GHAST(4.0F, 4.0F),
    PLAYER(1.8f, 0.6F),
    CROUCH(1.5f, 0.6F);

    @Getter
    private final float height, width;

    Boxes(float h, float w){
        this.width = w / 2.0f;
        this.height = h;
    }

}
