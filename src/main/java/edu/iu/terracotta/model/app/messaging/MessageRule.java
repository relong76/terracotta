package edu.iu.terracotta.model.app.messaging;

import edu.iu.terracotta.model.app.messaging.enums.MessageRuleComparison;
import edu.iu.terracotta.model.app.messaging.enums.MessageRuleField;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "terr_messaging_rule")
public class MessageRule  extends BaseMessageEntity {

    @ManyToOne
    @JoinColumn(
        name = "content_id",
        nullable = false
    )
    private MessageContent content;

    @Enumerated(EnumType.STRING)
    private MessageRuleField field;

    @Enumerated(EnumType.STRING)
    private MessageRuleComparison comparison;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "custom_placeholder_id")
    private MessageContentCustomPlaceholder customPlaceholder;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "standard_placeholder_id")
    private MessageContentStandardPlaceholder standardPlaceholder;

    @Column private String value;
    @Column private boolean enabled;

}
