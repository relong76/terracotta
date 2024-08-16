package edu.iu.terracotta.model.app.messaging;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Table(name = "terr_messaging_content")
public class MessageContent extends BaseMessageEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_id")
    private Message message;

    @Lob
    @Column
    private String body;

    @OneToMany(
        mappedBy = "content",
        fetch = FetchType.EAGER
    )
    private List<MessageContentCustomPlaceholder> customPlaceholders;

    @OneToMany(mappedBy = "content")
    private List<MessageRule> rules;

    @OneToMany(
        mappedBy = "content",
        fetch = FetchType.EAGER
    )
    private List<MessageContentAttachment> attachments;

}
