package me.liwk.karhu.replay.packet;


public class ReplayPacket {
    private final long timestamp;
    private final PacketDirection direction;
    private final PacketType packetType;
    private final PacketData data;

    public ReplayPacket(long timestamp, PacketDirection direction, PacketType packetType, PacketData data) {
        this.timestamp = timestamp;
        this.direction = direction;
        this.packetType = packetType;
        this.data = data;
    }

    public long getTimestamp() { return timestamp; }
    public PacketDirection getDirection() { return direction; }
    public PacketType getPacketType() { return packetType; }
    public PacketData getData() { return data; }
}
