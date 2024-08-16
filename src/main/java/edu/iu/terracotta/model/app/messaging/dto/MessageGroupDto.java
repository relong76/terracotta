package edu.iu.terracotta.model.app.messaging.dto;

import java.util.List;
import java.util.UUID;

import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageGroupDto {

    private UUID id;
    private long exposureId;
    private long ownerId;
    private List<MessageDto> messages;
    private MessageStatus status;
    private String title;
    private boolean toConsentedOnly;

}
