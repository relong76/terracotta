package edu.iu.terracotta.model.app.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "terr_messaging_email_reply_to")
public class MessageEmailReplyTo extends BaseMessageEntity {

    @ManyToOne
    @JoinColumn(
        name = "configuration_id",
        nullable = false
    )
    private MessageConfiguration configuration;

    @Column private String email;

}
