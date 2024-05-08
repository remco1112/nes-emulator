package org.example.nes.cpu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class CycleDeserializer extends JsonDeserializer<Cycle> {

    @Override
    public Cycle deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return new Cycle(
                node.get(2).asText().equals("read"),
                (short) node.get(0).intValue(),
                (byte) node.get(1).intValue()
        );
    }
}
