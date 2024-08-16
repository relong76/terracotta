package edu.iu.terracotta.model.app.messaging.dto;

import java.util.UUID;

import edu.iu.terracotta.model.app.messaging.enums.MessageRuleComparison;
import edu.iu.terracotta.model.app.messaging.enums.MessageRuleField;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageRuleDto {

    private UUID id;
    private MessageRuleField field;
    private MessageRuleComparison comparison;
    private String value;
    private boolean enabled;

}
