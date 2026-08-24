package me.liwk.karhu.handler.interfaces;

public interface KarhuHandler {
    void handleLastTicks();
    void handle(boolean moved);

    void cacheBlocks();
    void handleTicks();
    boolean hasCached();
}
