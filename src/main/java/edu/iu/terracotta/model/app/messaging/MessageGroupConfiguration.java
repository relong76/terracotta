package edu.iu.terracotta.model.app.messaging;

import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
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
@Table(name = "terr_messaging_message_group_configuration")
public class MessageGroupConfiguration extends BaseMessageEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private MessageGroup group;

    @Column
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @Column private int groupOrder;
    @Column private String title;
    @Column private boolean toConsentedOnly;

}
