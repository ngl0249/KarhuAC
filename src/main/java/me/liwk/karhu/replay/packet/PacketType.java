package me.liwk.karhu.replay.packet;

import me.liwk.karhu.replay.data.entity.DestroyEntitiesData;
import me.liwk.karhu.replay.data.entity.InitialEntityData;
import me.liwk.karhu.replay.data.entity.SpawnEntityData;
import me.liwk.karhu.replay.data.state.*;
import me.liwk.karhu.replay.data.world.BlockChangeData;
import me.liwk.karhu.replay.data.world.ChunkData;
import me.liwk.karhu.replay.data.world.InitialChunkData;
import me.liwk.karhu.replay.data.world.UnloadChunkData;

import java.util.HashMap;
import java.util.Map;

public enum PacketType {
    INITIAL_PLAYER_STATE(InitialPlayerStateData.class),
    INITIAL_ENTITY(InitialEntityData.class),
    INITIAL_INVENTORY(InitialInventoryData.class),
    INITIAL_CHUNK(InitialChunkData.class),
    PLAYER_POSITION(PlayerPositionData.class),
    PLAYER_ROTATION(PlayerRotationData.class),
    PLAYER_POSITION_AND_ROTATION(PlayerPosRotData.class),
    INTERACT_ENTITY(InteractEntityData.class),
    USE_ITEM(UseItemData.class),
    PLAYER_DIGGING(PlayerDiggingData.class),
    CHUNK_DATA(ChunkData.class),
    UNLOAD_CHUNK(UnloadChunkData.class),
    SPAWN_ENTITY(SpawnEntityData.class),
    DESTROY_ENTITIES(DestroyEntitiesData.class),
    BLOCK_CHANGE(BlockChangeData.class),
    ENTITY_VELOCITY(VelocityData.class),
    ENTITY_TELEPORT(TeleportData.class);

    private final Class<? extends PacketData> packetClass;

    PacketType(Class<? extends PacketData> packetClass) {
        this.packetClass = packetClass;
    }

    public Class<? extends PacketData> getPacketClass() {
        return packetClass;
    }

    // Optional: build reverse lookup map for class -> enum
    private static final Map<Class<? extends PacketData>, PacketType> classToTypeMap = new HashMap<>();
    static {
        for (PacketType type : values()) {
            classToTypeMap.put(type.packetClass, type);
        }
    }

    public static PacketType fromClass(Class<? extends PacketData> clazz) {
        return classToTypeMap.get(clazz);
    }
}