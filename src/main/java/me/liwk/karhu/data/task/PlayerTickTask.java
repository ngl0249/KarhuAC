package me.liwk.karhu.data.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.liwk.karhu.data.KarhuPlayer;

import java.util.Objects;

@Getter
public final class PlayerTickTask implements IAbstractTickTask<KarhuPlayer> {

    private final Runnable task;
    private final EmptyPredicate predicateUntil;

    private final String id;

    public PlayerTickTask(String id, Runnable task, EmptyPredicate predicateUntil) {
        this.id = "task-" + id;
        this.task = task;
        this.predicateUntil = predicateUntil;
    }

    @Override
    public Runnable getRunnable() {
        return this.task;
    }

    @Override
    public EmptyPredicate conditionUntil() {
        return this.predicateUntil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerTickTask)) return false;
        PlayerTickTask that = (PlayerTickTask) o;
        return Objects.equals(task, that.task) &&
                Objects.equals(predicateUntil, that.predicateUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, predicateUntil);
    }

}
