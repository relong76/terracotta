package edu.iu.terracotta.model.app.messaging.dto;

import java.util.UUID;

import edu.iu.terracotta.model.app.messaging.enums.MessageContentAttachmentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageContentAttachmentDto {

    private UUID id;
    private long canvasId;
    private String displayName;
    private String fileName;
    private long size;
    private String contentType;
    private String url;
    private MessageContentAttachmentStatus status;

}
