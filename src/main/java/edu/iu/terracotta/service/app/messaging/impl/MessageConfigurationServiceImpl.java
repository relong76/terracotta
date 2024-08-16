package edu.iu.terracotta.service.app.messaging.impl;

import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageConfiguration;
import edu.iu.terracotta.model.app.messaging.MessageEmailReplyTo;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageConfigurationDto;
import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import edu.iu.terracotta.model.app.messaging.enums.MessageType;
import edu.iu.terracotta.repository.messaging.MessageConfigurationRepository;
import edu.iu.terracotta.repository.messaging.MessageEmailReplyToRepository;
import edu.iu.terracotta.repository.messaging.MessageGroupRepository;
import edu.iu.terracotta.service.app.messaging.MessageConfigurationService;
import edu.iu.terracotta.service.app.messaging.MessageEmailReplyToService;

@Service
public class MessageConfigurationServiceImpl implements MessageConfigurationService {

    @Autowired private MessageConfigurationRepository messageConfigurationRepository;
    @Autowired private MessageEmailReplyToRepository messageEmailReplyToRepository;
    @Autowired private MessageGroupRepository messageGroupRepository;
    @Autowired private MessageEmailReplyToService messageEmailReplyToService;

    @Override
    public MessageConfigurationDto update(MessageConfigurationDto messageConfigurationDto, MessageGroup messageGroup, Message message, MessageConfiguration messageConfiguration) {
        fromDto(messageConfigurationDto, messageConfiguration);

        // set message status
        messageConfiguration.setStatus(isReadyToSend(messageConfiguration.getMessage()) ? MessageStatus.READY : MessageStatus.EDITED);

        // delete existing reply to addresses
        messageConfiguration.getReplyTo()
            .forEach(replyTo -> messageEmailReplyToRepository.deleteById(replyTo.getId()));

        // create new reply to addresses
        messageConfiguration.setReplyTo(
            CollectionUtils.emptyIfNull(messageConfigurationDto.getReplyTo()).stream()
                .map(
                    replyToDto ->
                        messageEmailReplyToRepository.save(
                            MessageEmailReplyTo.builder()
                                .configuration(messageConfiguration)
                                .email(replyToDto.getEmail())
                                .build()
                        )
                )
                .collect(Collectors.toList()) // need modifiable list
        );

        message.setConfiguration(
            messageConfigurationRepository.saveAndFlush(messageConfiguration)
        );

        // set group status to ready if all messages are ready
        boolean allMessagesReady = messageGroup.getMessages().stream()
            .allMatch(groupMessage -> groupMessage.getConfiguration().getStatus() == MessageStatus.READY);
        messageGroup.setStatus(allMessagesReady ? MessageStatus.READY : MessageStatus.EDITED);

        message.setGroup(
            messageGroupRepository.saveAndFlush(
                messageGroup
            )
        );

        return toDto(
            messageConfiguration,
            message.getUuid()
        );
    }

    @Override
    public MessageConfiguration duplicate(MessageConfiguration messageConfiguration) {
        MessageConfiguration newMessageConfiguration = messageConfigurationRepository.saveAndFlush(
            MessageConfiguration.builder()
                .sendAt(messageConfiguration.getSendAt())
                .status(MessageStatus.COPIED)
                .subject(messageConfiguration.getSubject())
                .type(messageConfiguration.getType())
                .build()
        );

        messageConfiguration.getReplyTo()
            .forEach(
                replyTo -> messageEmailReplyToService.duplicate(replyTo, newMessageConfiguration)
            );

        return  newMessageConfiguration;
    }

    @Override
    public MessageConfiguration copy(MessageConfiguration fromConfiguration, MessageConfiguration toConfiguration) {
        toConfiguration.setSendAt(fromConfiguration.getSendAt());
        toConfiguration.setStatus(MessageStatus.COPIED);
        toConfiguration.setSubject(fromConfiguration.getSubject());
        toConfiguration.setType(fromConfiguration.getType());

        fromConfiguration.getReplyTo()
            .forEach(
                replyTo -> messageEmailReplyToService.duplicate(replyTo, toConfiguration)
            );

        return messageConfigurationRepository.saveAndFlush(toConfiguration);
    }

    @Override
    public MessageConfigurationDto toDto(MessageConfiguration messageConfiguration, UUID messageUuid) {
        if (messageConfiguration == null) {
            return null;
        }

        return MessageConfigurationDto.builder()
            .id(messageConfiguration.getUuid())
            .messageId(messageUuid)
            .replyTo(
                messageEmailReplyToService.toDto(
                    messageConfiguration.getReplyTo()
                )
            )
            .sendAt(messageConfiguration.getSendAt())
            .status(messageConfiguration.getStatus())
            .subject(messageConfiguration.getSubject())
            .type(messageConfiguration.getType())
            .build();
    }

    @Override
    public MessageConfiguration fromDto(MessageConfigurationDto messageConfigurationDto, MessageConfiguration messageConfiguration) {
        messageConfiguration.setType(messageConfigurationDto.getType());
        messageConfiguration.setSubject(messageConfigurationDto.getSubject());
        messageConfiguration.setSendAt(messageConfigurationDto.getSendAt());

        return messageConfiguration;
    }

    /**
     * Message is ready to send if all are true:
     *
     * 1. content body is not blank
     * 2. sendAt is not blank
     * 3. subject is not blank
     * 4. type is not blank
     * 5. if email type, replyTo is not blank
     *
     * @param message
     * @return
     */
    private boolean isReadyToSend(Message message) {
        return !StringUtils.isAnyBlank(
            message.getContent().getBody(),
            message.getConfiguration().getSendAt() != null ? message.getConfiguration().getSendAt().toString() : null,
            message.getConfiguration().getSubject(),
            message.getConfiguration().getType().name()
        )
        &&
        !(message.getConfiguration().getType() == MessageType.EMAIL && StringUtils.isBlank(message.getConfiguration().getReplyTo().stream().map(MessageEmailReplyTo::getEmail).collect(Collectors.joining(","))));
    }

}
