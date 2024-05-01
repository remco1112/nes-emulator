package org.example.nes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RamDeserializer extends JsonDeserializer<Map<Integer, Byte>> {

    @Override
    public Map<Integer, Byte> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final Map<Integer, Byte> ramMap = new HashMap<>();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        for (var child : node) {
            ramMap.put(child.get(0).asInt(), (byte) child.get(1).asInt());
        }
        return ramMap;
    }
}
