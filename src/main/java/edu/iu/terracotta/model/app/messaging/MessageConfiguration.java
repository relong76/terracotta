package edu.iu.terracotta.model.app.messaging;

import java.sql.Timestamp;
import java.util.List;

import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import edu.iu.terracotta.model.app.messaging.enums.MessageType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "terr_messaging_message_configuration")
public class MessageConfiguration extends BaseMessageEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @OneToMany(
        mappedBy = "configuration",
        fetch = FetchType.EAGER
    )
    private List<MessageEmailReplyTo> replyTo;

    @Column private String subject;
    @Column private Timestamp sendAt;

}
