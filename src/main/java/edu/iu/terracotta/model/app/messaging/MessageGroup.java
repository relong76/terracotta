package edu.iu.terracotta.model.app.messaging;

import java.util.List;

import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.Exposure;
import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = "terr_messaging_message_group")
public class MessageGroup extends BaseMessageEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "exposure_id")
    private Exposure exposure;

    @OneToOne(mappedBy = "group")
    private MessageGroupConfiguration configuration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private LtiUserEntity owner;

    @OneToMany(
        mappedBy = "group",
        fetch = FetchType.EAGER
    )
    private List<Message> messages;

    @Transient
    public long getExposureId() {
        return exposure.getExposureId();
    }

    @Transient
    public MessageStatus getStatus() {
        return configuration.getStatus();
    }

}
