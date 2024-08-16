package edu.iu.terracotta.model.app.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "terr_messaging_content_custom_placeholder")
public class MessageContentCustomPlaceholder extends BaseMessageEntity {

    @ManyToOne
    @JoinColumn(
        name = "content_id",
        nullable = false
    )
    private MessageContent content;

    @OneToOne(mappedBy = "customPlaceholder")
    private MessageRule rule;

    @Column private String label;
    @Column private String key;
    @Column private boolean enabled;

}
