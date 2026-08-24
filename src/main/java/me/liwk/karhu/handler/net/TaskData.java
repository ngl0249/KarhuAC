package me.liwk.karhu.handler.net;

import lombok.AccessLevel;
import lombok.Getter;
import me.liwk.karhu.util.gui.Callback;

import java.util.LinkedList;
@Getter
public class TaskData {
    private final int id;
    private long timestamp;

    @Getter(AccessLevel.NONE)
    private final LinkedList<KarhuTask> tasks = new LinkedList<>();

    public TaskData(int id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public TaskData(int id, Callback<Integer> callback) {
        this.id = id;
        this.addTask(callback);
    }

    public void addTask(Callback<Integer> callback) {
        tasks.add(new KarhuTask(callback, id));
    }

    public void addTask(KarhuTask karhuTask) {
        karhuTask.setId(id);
        tasks.add(karhuTask);
    }

    public void consumeTask() {
        tasks.forEach(KarhuTask::runTask);
    }

    public boolean hasTask() {
        return tasks.size() > 0;
    }
}
