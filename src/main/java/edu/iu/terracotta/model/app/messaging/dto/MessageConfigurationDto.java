package edu.iu.terracotta.model.app.messaging.dto;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import edu.iu.terracotta.model.app.messaging.enums.MessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageConfigurationDto {

    private UUID id;
    private UUID messageId;
    private List<MessageEmailReplyToDto> replyTo;
    private Timestamp sendAt;
    private MessageStatus status;
    private String subject;
    private MessageType type;

}
