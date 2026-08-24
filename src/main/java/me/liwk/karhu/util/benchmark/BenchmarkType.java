package me.liwk.karhu.util.benchmark;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
public enum BenchmarkType {

    PHYSICS_SIMULATOR(100),
    BLOCK_COLLISION(100),
    CHECKS(100),
    BLOCK_CACHE(100),
    BB_CREATE(100),
    PLAY_RECEIVE(100),
    PLAY_SEND(100),
    TRANSACTION_TASK(100);

    private final int precision;
}
