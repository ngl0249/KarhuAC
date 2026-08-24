package me.liwk.karhu.handler.net;


import lombok.Getter;
import lombok.Setter;
import me.liwk.karhu.util.gui.Callback;

@Getter
@Setter
public class KarhuTask {
    private int id;
    private final Callback<Integer> callback;

    public KarhuTask(Callback<Integer> callback) {
        this.callback = callback;
    }

    public KarhuTask(Callback<Integer> callback, int id) {
        this.callback = callback;
        this.id = id;
    }

    public void runTask() {
        callback.call(id);
    }
}

