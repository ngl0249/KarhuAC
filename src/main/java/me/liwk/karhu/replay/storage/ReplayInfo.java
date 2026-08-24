package me.liwk.karhu.replay.storage;

public class ReplayInfo {
    private final String replayId;
    private final String playerName;
    private final long timestamp;
    private final String reason;
    private final long fileSize;

    public ReplayInfo(String replayId, String playerName, long timestamp, String reason, long fileSize) {
        this.replayId = replayId;
        this.playerName = playerName;
        this.timestamp = timestamp;
        this.reason = reason;
        this.fileSize = fileSize;
    }

    public String getReplayId() { return replayId; }
    public String getPlayerName() { return playerName; }
    public long getTimestamp() { return timestamp; }
    public String getReason() { return reason; }
    public long getFileSize() { return fileSize; }
}
