package edu.iu.terracotta.model.app.messaging;

import edu.iu.terracotta.model.app.messaging.enums.MessageContentAttachmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "terr_messaging_content_attachment")
public class MessageContentAttachment extends BaseMessageEntity {

    @ManyToOne
    @JoinColumn(
        name = "content_id",
        nullable = false
    )
    private MessageContent content;

    @Column
    @Enumerated(EnumType.STRING)
    private MessageContentAttachmentStatus status;

    @Column private long canvasId;
    @Column private String displayName;
    @Column private String fileName;
    @Column private long size;
    @Column private String contentType;
    @Column private String url;

}
