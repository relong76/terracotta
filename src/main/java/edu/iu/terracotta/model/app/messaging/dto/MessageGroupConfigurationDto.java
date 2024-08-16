package edu.iu.terracotta.model.app.messaging.dto;

import java.util.UUID;

import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageGroupConfigurationDto {

    private UUID id;
    private UUID groupId;
    private MessageStatus status;
    private String title;
    private boolean toConsentedOnly;
    private int order;

}
