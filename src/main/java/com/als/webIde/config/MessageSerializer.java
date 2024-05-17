package com.als.webIde.config;

import com.als.webIde.DTO.response.Message;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class MessageSerializer extends JsonSerializer<Message> {
    @Override
    public void serialize(Message message, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("message", message.getMessage());
        jsonGenerator.writeEndObject();
    }
}
