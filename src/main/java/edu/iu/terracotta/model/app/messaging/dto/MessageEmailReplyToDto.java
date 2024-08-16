package edu.iu.terracotta.model.app.messaging.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageEmailReplyToDto {

    private UUID id;
    private UUID configurationId;
    private String email;

}
