package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.repository.messaging.MessageRepository;
import edu.iu.terracotta.service.app.messaging.MessageConfigurationService;
import edu.iu.terracotta.service.app.messaging.MessageContentService;
import edu.iu.terracotta.service.app.messaging.MessageService;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageServiceImpl implements MessageService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private MessageConfigurationService messageConfigurationService;
    @Autowired private MessageContentService messageContentService;

    @Override
    public MessageDto get(Message message) {
        return toDto(message);
    }

    @Override
    public MessageDto update(MessageDto messageDto, long exposureId, MessageGroup messageGroup, Message message) {
        return toDto(
            messageRepository.save(message)
        );
    }

    @Override
    public Message duplicate(Message message, MessageGroup messageGroup, SecuredInfo securedInfo) {
        Message newMessage = messageRepository.saveAndFlush(
            Message.builder()
                .exposureGroupCondition(message.getExposureGroupCondition())
                .group(messageGroup)
                .build()
        );

        messageConfigurationService.duplicate(message.getConfiguration(), newMessage);
        messageContentService.duplicate(message.getContent(), newMessage, securedInfo);

        return messageRepository.saveAndFlush(newMessage);
    }

    @Override
    public List<MessageDto> toDto(List<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }

        return messages.stream()
            .map(message -> toDto(message))
            .toList();
    }

    @Override
    public MessageDto toDto(Message message) {
        if (message ==  null) {
            return null;
        }

        return MessageDto.builder()
            .id(message.getUuid())
            .exposureGroupConditionId(message.getExposureGroupConditionId())
            .conditionId(message.getConditionId())
            .configuration(
                messageConfigurationService.toDto(message.getConfiguration(), message.getUuid())
            )
            .content(
                messageContentService.toDto(message.getContent())
            )
            .created(message.getCreatedAt())
            .groupId(message.getGroup().getUuid())
            .ownerEmail(message.getOwner().getEmail())
            .build();
    }

}
