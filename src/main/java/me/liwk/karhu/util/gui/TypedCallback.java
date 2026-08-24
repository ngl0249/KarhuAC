package me.liwk.karhu.util.gui;

@FunctionalInterface
public interface TypedCallback<T> {

    void execute(T type);

}
