package edu.iu.terracotta.model.app.messaging;

import edu.iu.terracotta.model.app.messaging.enums.MessageContentStandardPlaceholderKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "terr_messaging_content_standard_placeholder")
public class MessageContentStandardPlaceholder extends BaseMessageEntity {

    @Column
    @Enumerated(EnumType.STRING)
    private MessageContentStandardPlaceholderKey key;

    @OneToOne(mappedBy = "standardPlaceholder")
    private MessageRule rule;

    @Column private String label;
    @Column private boolean enabled;

}
