package edu.iu.terracotta.service.app.messaging;

import java.util.List;
import java.util.UUID;

import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageRuleNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageRuleNotMatchingException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.MessageRule;
import edu.iu.terracotta.model.app.messaging.dto.MessageRuleDto;

public interface MessageRuleService {

    MessageRuleDto create(MessageRuleDto messageRuleDto, Message message, UUID contentUuid) throws MessageContentNotFoundException, MessageContentNotMatchingException;
    MessageRuleDto update(MessageRuleDto messageRuleDto, Message message, UUID contentUuid, UUID uuid) throws MessageContentNotFoundException, MessageContentNotMatchingException, MessageRuleNotFoundException, MessageRuleNotMatchingException;
    void delete(Message message, UUID contentUuid, UUID uuid);
    MessageRule duplicate(MessageRule messageRule, MessageContent messageContent);
    List<MessageRuleDto> toDto(List<MessageRule> messageContentCustomFields, UUID messageContentUuid);
    MessageRuleDto toDto(MessageRule messageRule, UUID contentUuid);
    MessageRule fromDto(MessageRuleDto messageRuleDto, MessageRule messageRule);

}
