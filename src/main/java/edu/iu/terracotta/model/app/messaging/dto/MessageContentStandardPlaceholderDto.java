package edu.iu.terracotta.model.app.messaging.dto;

import java.util.UUID;

import edu.iu.terracotta.model.app.messaging.enums.MessageContentStandardPlaceholderKey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageContentStandardPlaceholderDto {

    private UUID id;
    private String contentId;
    private String label;
    private MessageContentStandardPlaceholderKey key;
    private boolean enabled;

}
