package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.messaging.MessageGroupConfigurationNotFoundException;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.MessageGroupConfiguration;
import edu.iu.terracotta.model.app.messaging.dto.MessageGroupConfigurationDto;
import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import edu.iu.terracotta.repository.messaging.MessageGroupConfigurationRepository;
import edu.iu.terracotta.service.app.messaging.MessageGroupConfigurationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageGroupConfigurationServiceImpl implements MessageGroupConfigurationService {

    @Autowired private MessageGroupConfigurationRepository messageGroupConfigurationRepository;

    @Override
    public MessageGroupConfiguration create(MessageGroup messageGroup) {
        return messageGroupConfigurationRepository.saveAndFlush(
            MessageGroupConfiguration.builder()
                .group(messageGroup)
                .groupOrder(1)
                .toConsentedOnly(false)
                .status(MessageStatus.CREATED)
                .build()
        );
    }

    @Override
    public MessageGroupConfigurationDto update(MessageGroupConfigurationDto messageGroupConfigurationDto, MessageGroupConfiguration messageGroupConfiguration) {
        fromDto(messageGroupConfigurationDto, messageGroupConfiguration);

        return toDto(
            messageGroupConfigurationRepository.saveAndFlush(messageGroupConfiguration)
        );
    }

    @Override
    public List<MessageGroupConfigurationDto> updateAll(List<MessageGroupConfigurationDto> messageGroupConfigurationDtos, List<MessageGroupConfiguration> messageGroupConfigurations) {
        return messageGroupConfigurationDtos.stream()
            .map(
                messageGroupConfigurationDto -> {
                    try {
                        return update(
                            messageGroupConfigurationDto,
                            messageGroupConfigurations.stream()
                                .filter(messageGroupConfiguration -> messageGroupConfiguration.getUuid().equals(messageGroupConfigurationDto.getId()))
                                .findFirst()
                                .orElseThrow(() -> new MessageGroupConfigurationNotFoundException(String.format("No message group configuration with UUID: [%s] found.", messageGroupConfigurationDto.getId())))
                        );
                    } catch (MessageGroupConfigurationNotFoundException e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }
            )
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public MessageGroupConfiguration duplicate(MessageGroupConfiguration messageGroupConfiguration, MessageGroup messageGroup) {
        return messageGroupConfigurationRepository.saveAndFlush(
            MessageGroupConfiguration.builder()
                .group(messageGroup)
                .groupOrder(messageGroupConfiguration.getGroupOrder())
                .status(MessageStatus.COPIED)
                .title(String.format("Copy of %s", messageGroupConfiguration.getTitle()))
                .toConsentedOnly(messageGroupConfiguration.isToConsentedOnly())
                .build()
        );
    }

    @Override
    public MessageGroupConfigurationDto toDto(MessageGroupConfiguration messageGroupConfiguration) {
        if (messageGroupConfiguration == null) {
            return null;
        }

        return MessageGroupConfigurationDto.builder()
            .groupId(messageGroupConfiguration.getGroup().getUuid())
            .id(messageGroupConfiguration.getUuid())
            .order(messageGroupConfiguration.getGroupOrder())
            .status(messageGroupConfiguration.getStatus())
            .title(messageGroupConfiguration.getTitle())
            .toConsentedOnly(messageGroupConfiguration.isToConsentedOnly())
            .build();
    }

    @Override
    public MessageGroupConfiguration fromDto(MessageGroupConfigurationDto messageGroupConfigurationDto, MessageGroupConfiguration messageGroupConfiguration) {
        if (messageGroupConfiguration == null) {
            messageGroupConfiguration = MessageGroupConfiguration.builder().build();
        }

        if (messageGroupConfigurationDto == null) {
            return messageGroupConfiguration;
        }

        messageGroupConfiguration.setGroupOrder(messageGroupConfigurationDto.getOrder());
        messageGroupConfiguration.setTitle(messageGroupConfigurationDto.getTitle());
        messageGroupConfiguration.setToConsentedOnly(messageGroupConfigurationDto.isToConsentedOnly());

        return messageGroupConfiguration;
    }

}
