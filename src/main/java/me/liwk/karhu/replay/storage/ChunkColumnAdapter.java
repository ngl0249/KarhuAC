package me.liwk.karhu.replay.storage;

import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChunkColumnAdapter implements JsonSerializer<Column>, JsonDeserializer<Column> {

    @Override
    public JsonElement serialize(Column src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("x", src.getX());
        result.addProperty("z", src.getZ());
        // Note: In a real implementation, you'd serialize the chunk data
        // For now, we'll just store coordinates as chunks are complex
        result.addProperty("simplified", true);
        return result;
    }

    @Override
    public Column deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        int x = jsonObject.get("x").getAsInt();
        int z = jsonObject.get("z").getAsInt();

        // Create a basic empty column - in practice you'd reconstruct the full chunk
        return new Column(x, z, true, (BaseChunk[]) null, (TileEntity[]) null, (int[]) null);
    }
}
