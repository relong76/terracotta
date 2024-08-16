package edu.iu.terracotta.model.app.messaging.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageContentCustomPlaceholderDto {

    private UUID id;
    private String contentId;
    private String label;
    private String key;
    private boolean enabled;

}
