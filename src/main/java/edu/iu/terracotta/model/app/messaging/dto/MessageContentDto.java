package edu.iu.terracotta.model.app.messaging.dto;

import java.util.UUID;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageContentDto {

    private UUID id;
    private UUID messageId;
    private String body;
    private List<MessageContentCustomPlaceholderDto> customPlaceholders;
    private List<MessageContentStandardPlaceholderDto> standardPlaceholders;
    private List<MessageRuleDto> rules;
    private List<MessageContentAttachmentDto> attachments;

}
