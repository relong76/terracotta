package edu.iu.terracotta.service.app.messaging.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.messaging.MessageGroupMoveException;
import edu.iu.terracotta.model.app.Exposure;
import edu.iu.terracotta.model.app.ExposureGroupCondition;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageConfiguration;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageGroupDto;
import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.repository.ExposureGroupConditionRepository;
import edu.iu.terracotta.repository.LtiUserRepository;
import edu.iu.terracotta.repository.messaging.MessageConfigurationRepository;
import edu.iu.terracotta.repository.messaging.MessageContentRepository;
import edu.iu.terracotta.repository.messaging.MessageGroupConfigurationRepository;
import edu.iu.terracotta.repository.messaging.MessageGroupRepository;
import edu.iu.terracotta.repository.messaging.MessageRepository;
import edu.iu.terracotta.service.app.messaging.MessageGroupConfigurationService;
import edu.iu.terracotta.service.app.messaging.MessageGroupService;
import edu.iu.terracotta.service.app.messaging.MessageService;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageGroupServiceImpl implements MessageGroupService {

    @Autowired private ExposureGroupConditionRepository exposureGroupConditionRepository;
    @Autowired private LtiUserRepository ltiUserRepository;
    @Autowired private MessageConfigurationRepository messageConfigurationRepository;
    @Autowired private MessageContentRepository messageContentRepository;
    @Autowired private MessageGroupConfigurationRepository messageGroupConfigurationRepository;
    @Autowired private MessageGroupRepository messageGroupRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private MessageGroupConfigurationService messageGroupConfigurationService;
    @Autowired private MessageService messageService;

    @Override
    public MessageGroupDto get(MessageGroup messageGroup) {
        return toDto(messageGroup);
    }

    @Override
    public List<MessageGroupDto> getAll(long experimentId, long exposureId, SecuredInfo securedInfo) {
        return toDto(
            messageGroupRepository.findAllByExposure_Experiment_ExperimentIdAndExposure_ExposureIdAndOwner_LmsUserId(
                experimentId,
                exposureId,
                securedInfo.getCanvasUserId()
            )
        );
    }

    @Override
    public MessageGroupDto create(Exposure exposure, boolean single, SecuredInfo securedInfo) {
        MessageGroup messageGroup = messageGroupRepository.saveAndFlush(
            MessageGroup.builder()
                .exposure(exposure)
                .owner(ltiUserRepository.findByUserKeyAndPlatformDeployment_KeyId(securedInfo.getUserId(), securedInfo.getPlatformDeploymentId()))
                .build()
        );

        messageGroup.setConfiguration(
            messageGroupConfigurationService.create(messageGroup)
        );

        List<ExposureGroupCondition> exposureGroupConditions = exposureGroupConditionRepository.findByExposure_ExposureId(exposure.getExposureId());

        // create messages
        List<Message> messages;

        if (single) {
            // single-version message; use default exposureGroupCondition
            messages = exposureGroupConditions.stream()
                .filter(exposureGroupCondition -> BooleanUtils.isTrue(exposureGroupCondition.getCondition().getDefaultCondition()))
                .map(exposureGroupCondition ->
                    messageRepository.saveAndFlush(
                        Message.builder()
                            .exposureGroupCondition(exposureGroupCondition)
                            .group(messageGroup)
                            .build()
                    )
                )
                .toList();
        } else {
            // multiple-version message; create for each exposureGroupCondition
            messages = exposureGroupConditions.stream()
                .map(exposureGroupCondition ->
                    messageRepository.saveAndFlush(
                        Message.builder()
                            .exposureGroupCondition(exposureGroupCondition)
                            .group(messageGroup)
                            .build()
                    )
                )
                .toList();
        }

        // create message configurations
        messages.forEach(
            message -> {
                message.setConfiguration(
                    messageConfigurationRepository.saveAndFlush(
                        MessageConfiguration.builder()
                            .message(message)
                            .sendAt(Timestamp.from(Instant.now()))
                            .status(MessageStatus.CREATED)
                            .build()
                    )
                );
            }
        );

        // create message contents
        messages.forEach(
            message -> {
                message.setContent(
                    messageContentRepository.saveAndFlush(
                        MessageContent.builder()
                            .message(message)
                            .build()
                    )
                );
            }
        );

        messageGroup.setMessages(
            messages.stream()
                .map(message -> messageRepository.saveAndFlush(message))
                .toList()
        );

        return toDto(messageGroupRepository.findById(messageGroup.getId()).get());
    }

    @Override
    public MessageGroupDto update(MessageGroupDto messageGroupDto, MessageGroup messageGroup) {
        if (messageGroup.getStatus() == MessageStatus.QUEUED) {
            // message group queued for sending; mark each message as queued
            messageGroup.getMessages().stream()
                .filter(message -> message.getConfiguration() != null)
                .filter(message -> message.getContent() != null)
                .forEach(
                    message -> {
                        message.getConfiguration().setStatus(MessageStatus.QUEUED);
                        message.setConfiguration(
                            messageConfigurationRepository.saveAndFlush(message.getConfiguration())
                        );
                    }
                );
        }

        return toDto(messageGroupRepository.findById(messageGroup.getId()).get());
    }

    @Override
    public MessageGroupDto send(MessageGroup messageGroup) {
        messageGroup.getConfiguration().setStatus(MessageStatus.QUEUED);
        messageGroupConfigurationRepository.saveAndFlush(messageGroup.getConfiguration());

        // set all messages as QUEUED for sending
        messageGroup.getMessages().stream()
            .filter(message -> message.getConfiguration() != null)
            .filter(message -> message.getContent() != null)
            .forEach(
                message -> {
                    message.getConfiguration().setStatus(MessageStatus.QUEUED);
                    messageConfigurationRepository.saveAndFlush(message.getConfiguration());
                }
            );

        return toDto(messageGroup);
    }

    @Override
    public MessageGroupDto delete(MessageGroup messageGroup) {
        messageGroup.getConfiguration().setStatus(MessageStatus.DELETED);
        messageGroupConfigurationRepository.saveAndFlush(messageGroup.getConfiguration());

        messageGroup.getMessages().stream()
            .forEach(
                message -> {
                    if (message.getConfiguration() == null) {
                        message.setConfiguration(
                            messageConfigurationRepository.saveAndFlush(
                                MessageConfiguration.builder()
                                    .message(message)
                                    .status(MessageStatus.DELETED)
                                    .build()
                            )
                        );
                        messageRepository.saveAndFlush(message);

                        return;
                    }

                    message.getConfiguration().setStatus(MessageStatus.DELETED);
                    messageConfigurationRepository.saveAndFlush(message.getConfiguration());
                }
            );

        return toDto(messageGroupRepository.findById(messageGroup.getId()).get());
    }

    @Override
    public MessageGroupDto move(Exposure exposure, MessageGroup messageGroup) {
        List<ExposureGroupCondition> newExposureGroupConditions = exposureGroupConditionRepository.findByExposure_ExposureId(exposure.getExposureId());

        messageGroup.setExposure(exposure);
        messageGroupRepository.saveAndFlush(messageGroup);

        messageGroup.getMessages()
            .forEach(
                message -> {
                    try {
                        // set the message to the new exposureGroupCondition with the same condition as the old exposureGroupCondition
                        message.setExposureGroupCondition(
                            newExposureGroupConditions.stream()
                                .filter(egc -> egc.getCondition().getConditionId().equals(message.getConditionId()))
                                .findFirst()
                                .orElseThrow(
                                    () -> new MessageGroupMoveException(
                                        String.format(
                                            "No exposureGroupCondition found with condition ID: [%s] and group ID: [%s]",
                                            message.getConditionId(),
                                            message.getGroupId()
                                        )
                                    )
                            )
                        );
                    } catch (MessageGroupMoveException e) {
                        log.error(e.getMessage(), e);
                        return;
                    }

                    messageRepository.saveAndFlush(message);
                }
            );

        return toDto(messageGroupRepository.findById(messageGroup.getId()).get());
    }

    @Override
    public MessageGroupDto duplicate(MessageGroup messageGroup, Exposure exposure, SecuredInfo securedInfo) {
        MessageGroup newMessageGroup = messageGroupRepository.saveAndFlush(
            MessageGroup.builder()
                .exposure(exposure)
                .owner(messageGroup.getOwner())
                .build()
        );

        messageGroupConfigurationService.duplicate(messageGroup.getConfiguration(), newMessageGroup);
        messageGroup.getMessages().stream()
            .map(message -> messageService.duplicate(message, newMessageGroup, securedInfo))
            .toList();

        return toDto(messageGroupRepository.saveAndFlush(newMessageGroup));
    }

    @Override
    public List<MessageGroupDto> toDto(List<MessageGroup> messageGroups) {
        if (CollectionUtils.isEmpty(messageGroups)) {
            return Collections.emptyList();
        }

        return messageGroups.stream()
            .filter(messageGroup -> isDeployable(messageGroup))
            .map(messageGroup -> toDto(messageGroup))
            .toList();
    }

    @Override
    public MessageGroupDto toDto(MessageGroup messageGroup) {
        if (messageGroup == null) {
            return null;
        }

        return MessageGroupDto.builder()
            .configuration(messageGroupConfigurationService.toDto(messageGroup.getConfiguration()))
            .exposureId(messageGroup.getExposure().getExposureId())
            .id(messageGroup.getUuid())
            .messages(messageService.toDto(messageGroup.getMessages()))
            .ownerId(messageGroup.getOwner().getUserId())
            .build();
    }

    private boolean isDeployable(MessageGroup messageGroup) {
        return MessageStatus.displayable().contains(messageGroup.getConfiguration().getStatus());
    }

}
