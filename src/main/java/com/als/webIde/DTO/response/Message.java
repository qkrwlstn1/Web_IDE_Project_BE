package com.als.webIde.DTO.response;

import com.als.webIde.config.MessageSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonSerialize(using = MessageSerializer.class)
public class Message {
    private String message;
}
