package me.liwk.karhu.event;

public class SwingEvent extends Event {

    private final long nanoTime, timeMillis;
    private final int recordSwing;

    public SwingEvent(long nanoTime, long timeMillis, int recordSwing) {
        this.nanoTime = nanoTime;
        this.timeMillis = timeMillis;
        this.recordSwing = recordSwing;
    }

    public int getRecordSwing() {
        return recordSwing;
    }

    public long getTimeStamp() {
        return nanoTime;
    }

    public long getTimeStampMS() {
        return timeMillis;
        //return (long) (timeStamp / 1E6);
    }

}
