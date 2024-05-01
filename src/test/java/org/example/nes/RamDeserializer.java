package org.example.nes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class RamDeserializer extends JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final byte[] ram = new byte[0x10000];
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        for (var child : node) {
            ram[child.get(0).asInt()] = (byte) child.get(1).asInt();
        }
        return ram;
    }
}
