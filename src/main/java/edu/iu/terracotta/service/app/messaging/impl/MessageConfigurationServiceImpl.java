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
import edu.iu.terracotta.repository.messaging.MessageGroupConfigurationRepository;
import edu.iu.terracotta.repository.messaging.MessageGroupRepository;
import edu.iu.terracotta.service.app.messaging.MessageConfigurationService;
import edu.iu.terracotta.service.app.messaging.MessageEmailReplyToService;

@Service
public class MessageConfigurationServiceImpl implements MessageConfigurationService {

    @Autowired private MessageConfigurationRepository messageConfigurationRepository;
    @Autowired private MessageEmailReplyToRepository messageEmailReplyToRepository;
    @Autowired private MessageGroupConfigurationRepository messageGroupConfigurationRepository;
    @Autowired private MessageGroupRepository messageGroupRepository;
    @Autowired private MessageEmailReplyToService messageEmailReplyToService;

    @Override
    public MessageConfigurationDto update(MessageConfigurationDto messageConfigurationDto, MessageGroup messageGroup, Message message, MessageConfiguration messageConfiguration) {
        fromDto(
            messageConfigurationDto,
            messageConfiguration
        );

        // set message status
        messageConfiguration.setStatus(isReadyToSend(messageConfiguration.getMessage()) ? MessageStatus.READY : MessageStatus.EDITED);

        // delete existing reply to addresses
        messageConfiguration.getReplyTo()
            .forEach(replyTo -> messageEmailReplyToRepository.deleteById(replyTo.getId()));

        if (messageConfiguration.getType() == MessageType.EMAIL) {
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
        }

        messageConfigurationRepository.saveAndFlush(messageConfiguration);

        // set group status to ready if all messages are ready
        boolean allMessagesReady = messageGroup.getMessages().stream()
            .allMatch(groupMessage -> groupMessage.getStatus() == MessageStatus.READY);
        messageGroup.getConfiguration().setStatus(allMessagesReady ? MessageStatus.READY : MessageStatus.EDITED);
        messageGroupConfigurationRepository.saveAndFlush(messageGroup.getConfiguration());
        messageGroupRepository.saveAndFlush(messageGroup);

        return toDto(
            messageConfigurationRepository.findById(messageConfiguration.getId()).get(),
            message.getUuid()
        );
    }

    @Override
    public MessageConfiguration duplicate(MessageConfiguration messageConfiguration, Message message) {
        MessageConfiguration newMessageConfiguration = messageConfigurationRepository.saveAndFlush(
            MessageConfiguration.builder()
                .message(message)
                .sendAt(messageConfiguration.getSendAt())
                .status(MessageStatus.COPIED)
                .subject(messageConfiguration.getSubject())
                .type(messageConfiguration.getType())
                .build()
        );

        CollectionUtils.emptyIfNull(messageConfiguration.getReplyTo())
            .forEach(replyTo -> messageEmailReplyToService.duplicate(replyTo, newMessageConfiguration));

        return messageConfigurationRepository.saveAndFlush(newMessageConfiguration);
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
     * 5. if email type, replyTo(s) are not blank
     *
     * @param message
     * @return
     */
    private boolean isReadyToSend(Message message) {
        if (
            StringUtils.isAnyBlank(
                message.getContent().getBody(),
                message.getConfiguration().getSendAt() != null ? message.getConfiguration().getSendAt().toString() : null,
                message.getConfiguration().getSubject(),
                message.getConfiguration().getType().name()
            )
        ) {
            return false;
        }

        return message.getConfiguration().getType() == MessageType.CONVERSATION ||
            (
                message.getConfiguration().getType() == MessageType.EMAIL &&
                message.getConfiguration().getReplyTo().stream().allMatch(replyTo -> StringUtils.isNotBlank(replyTo.getEmail()))
            );
    }

}
