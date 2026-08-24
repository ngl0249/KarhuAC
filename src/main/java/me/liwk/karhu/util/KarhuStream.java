package me.liwk.karhu.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Mostly utility for faster stream stuff
 *
 * @param <T>
 */
public final class KarhuStream<T> {

    private List<T> list;

    public KarhuStream(final Collection<T> collection) {
        this.list = new ArrayList<>(collection); // Ensure it's a list for faster indexed access
    }

    public boolean any(final Predicate<T> predicate) {
        return list.stream().anyMatch(predicate); // Leverage Java 8's optimized Stream API
    }

    public boolean all(final Predicate<T> p) {
        for (final T t : this.list) {
            if (!p.test(t)) {
                return false;
            }
        }
        return true;
    }

    public T find(final Predicate<T> p) {
        for (final T t : this.list) {
            if (p.test(t)) {
                return t;
            }
        }
        return null;
    }

    public void setCollection(List<T> list) {
        this.list = list;
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public void forEach(Consumer<? super T> consumer) {
        this.list.forEach(consumer);
    }

}
