package me.liwk.karhu.replay.storage;

import com.google.gson.*;
import me.liwk.karhu.replay.data.state.*;
import me.liwk.karhu.replay.packet.PacketData;
import me.liwk.karhu.replay.packet.PacketType;

import java.lang.reflect.Type;

public class PacketDataAdapter implements JsonSerializer<PacketData>, JsonDeserializer<PacketData> {

    @Override
    public JsonElement serialize(PacketData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        PacketType type = PacketType.fromClass(src.getClass());
        if (type == null) throw new JsonParseException("Unknown PacketData class: " + src.getClass());

        // Store numeric ID (ordinal) instead of string
        result.add("type", new JsonPrimitive(type.ordinal()));
        result.add("properties", context.serialize(src, src.getClass()));
        return result;
    }

    @Override
    public PacketData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        int ordinal = jsonObject.get("type").getAsInt();
        JsonElement element = jsonObject.get("properties");

        if (ordinal < 0 || ordinal >= PacketType.values().length) {
            throw new JsonParseException("Invalid packet type ordinal: " + ordinal);
        }

        PacketType type = PacketType.values()[ordinal];
        return context.deserialize(element, type.getPacketClass());
    }
}
