package edu.iu.terracotta.model.app.messaging;

import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.enums.MessageProcessingStatus;
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
@Table(name = "terr_messaging_message_log")
public class MessageLog extends BaseMessageEntity {

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "message_id",
        nullable = false
    )
    private Message message;

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "lti_user_user_id",
        nullable = false
    )
    private LtiUserEntity recipient;

    @Column
    @Enumerated(EnumType.STRING)
    private MessageProcessingStatus status;

    @Column private long canvasConversationId;
    @Column private String body;

}
