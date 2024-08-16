package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageRuleNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageRuleNotMatchingException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.MessageRule;
import edu.iu.terracotta.model.app.messaging.dto.MessageRuleDto;
import edu.iu.terracotta.repository.messaging.MessageContentRepository;
import edu.iu.terracotta.repository.messaging.MessageRuleRepository;
import edu.iu.terracotta.service.app.messaging.MessageRuleService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageRuleServiceImpl implements MessageRuleService {

    @Autowired private MessageContentRepository messageContentRepository;
    @Autowired private MessageRuleRepository messageRuleRepository;

    @Override
    public MessageRuleDto create(MessageRuleDto messageRuleDto, Message message, UUID contentUuid) throws MessageContentNotFoundException, MessageContentNotMatchingException {
        if (!message.getContent().getUuid().equals(contentUuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", contentUuid, message.getId()));
        }

        MessageContent messageContent = messageContentRepository.findByUuid(contentUuid)
            .orElseThrow(() -> new MessageContentNotFoundException(String.format("No message content with UUID: [%s] found.", contentUuid)));

        return toDto(
            messageRuleRepository.save(
                MessageRule.builder()
                .comparison(messageRuleDto.getComparison())
                .content(messageContent)
                .field(messageRuleDto.getField())
                .value(messageRuleDto.getValue())
                .build()
            ),
            contentUuid
        );
    }

    @Override
    public MessageRuleDto update(MessageRuleDto messageRuleDto, Message message, UUID contentUuid, UUID uuid)
        throws MessageContentNotFoundException, MessageContentNotMatchingException, MessageRuleNotFoundException, MessageRuleNotMatchingException {
        if (!message.getContent().getUuid().equals(contentUuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", contentUuid, message.getId()));
        }

        MessageContent messageContent = messageContentRepository.findByUuid(contentUuid)
            .orElseThrow(() -> new MessageContentNotFoundException(String.format("No message content with UUID: [%s] found.", contentUuid)));

        boolean hasRule = messageContent.getRules().stream()
            .anyMatch(rule -> rule.getUuid().equals(uuid));

        if (!hasRule) {
            throw new MessageRuleNotMatchingException(String.format("Message rule UUID: [%s] is not associated with message content with ID: [%s]", uuid, messageContent.getId()));
        }

        return toDto(
            fromDto(messageRuleDto,
                messageRuleRepository.findByUuid(uuid)
                    .orElseThrow(() -> new MessageRuleNotFoundException(String.format("No message rule with UUID: [%s] found.", uuid)))
            ),
            contentUuid
        );
    }

    @Override
    public void delete(Message message, UUID contentUuid, UUID uuid) {
        if (!message.getContent().getUuid().equals(contentUuid)) {
            log.error("Message content with UUID: [%s] does not match the given message with ID: [{}].", contentUuid, message.getId());
            return;
        }

        Optional<MessageContent> messageContent = messageContentRepository.findByUuid(contentUuid);

        if (messageContent.isEmpty()) {
            log.error("No message content with UUID: [{}] found.", contentUuid);
            return;
        }

        boolean hasRule = messageContent.get().getRules().stream()
            .anyMatch(rule -> rule.getUuid().equals(uuid));

        if (!hasRule) {
            log.error("Message rule UUID: [{}] is not associated with message content with ID: [{}]", uuid, messageContent.get().getId());
            return;
        }

        Optional<MessageRule> messageRule = messageRuleRepository.findByUuid(uuid);

        if (messageRule.isEmpty()) {
            log.error("No message rule with UUID: [{}] found.", uuid);
            return;
        }

        messageRuleRepository.deleteById(messageRule.get().getId());
    }

    @Override
    public MessageRule duplicate(MessageRule messageRule, MessageContent messageContent) {
        return messageRuleRepository.saveAndFlush(
            MessageRule.builder()
                .comparison(messageRule.getComparison())
                .content(messageContent)
                .customPlaceholder(messageRule.getCustomPlaceholder())
                .enabled(messageRule.isEnabled())
                .field(messageRule.getField())
                .standardPlaceholder(messageRule.getStandardPlaceholder())
                .value(messageRule.getValue())
                .build()
        );
    }

    @Override
    public List<MessageRuleDto> toDto(List<MessageRule> messageRules, UUID messageContentUuid) {
        return CollectionUtils.emptyIfNull(messageRules).stream()
            .map(messageRule -> toDto(messageRule, messageContentUuid))
            .toList();
    }

    @Override
    public MessageRuleDto toDto(MessageRule messageRule, UUID messageContentUuid) {
        if (messageRule == null) {
            return null;
        }

        return MessageRuleDto.builder()
            .id(messageRule.getUuid())
            .comparison(messageRule.getComparison())
            .enabled(messageRule.isEnabled())
            .field(messageRule.getField())
            .value(messageRule.getValue())
            .build();
    }

    @Override
    public MessageRule fromDto(MessageRuleDto messageRuleDto, MessageRule messageRule) {
        messageRule.setComparison(messageRuleDto.getComparison());
        messageRule.setEnabled(messageRuleDto.isEnabled());
        messageRule.setField(messageRuleDto.getField());
        messageRule.setValue(messageRuleDto.getValue());

        return messageRule;
    }

}
