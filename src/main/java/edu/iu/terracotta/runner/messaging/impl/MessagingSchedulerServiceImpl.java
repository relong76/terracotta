package edu.iu.terracotta.runner.messaging.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import edu.iu.terracotta.repository.messaging.MessageConfigurationRepository;
import edu.iu.terracotta.repository.messaging.MessageGroupConfigurationRepository;
import edu.iu.terracotta.repository.messaging.MessageRepository;
import edu.iu.terracotta.runner.messaging.MessagingSchedulerService;
import edu.iu.terracotta.runner.messaging.model.MessagingScheduleMessage;
import edu.iu.terracotta.runner.messaging.model.MessagingScheduleResult;
import edu.iu.terracotta.service.app.messaging.MessageConversationService;
import edu.iu.terracotta.service.app.messaging.MessageEmailService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessagingSchedulerServiceImpl implements MessagingSchedulerService {

    @Autowired private MessageConfigurationRepository messageConfigurationRepository;
    @Autowired private MessageGroupConfigurationRepository messageGroupConfigurationRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private MessageConversationService messageConversationService;
    @Autowired private MessageEmailService messageEmailService;

    @Override
    public Optional<MessagingScheduleResult> send() {
        List<Message> messages = messageRepository.findByConfiguration_StatusAndConfiguration_SendAtLessThan(
            MessageStatus.QUEUED,
            Timestamp.valueOf(LocalDateTime.now())
        );

        // set each message status to "processing"
        messages.forEach(
            message -> {
                message.getConfiguration().setStatus(MessageStatus.PROCESSING);
                message.setConfiguration(
                    messageConfigurationRepository.save(message.getConfiguration())
                );
                // set group status
                message.getGroup().getConfiguration().setStatus(MessageStatus.PROCESSING);
                //messageGroupRepository.save(message.getGroup());
            }
        );

        if (CollectionUtils.isEmpty(messages)) {
            return Optional.empty();
        }

        List<MessagingScheduleMessage> processed = messages.stream()
            .map(
                message -> {
                    log.info("Messaging scheduler processing [{}] message ID: [{}]", message.getConfiguration().getType(), message.getId());
                    MessagingScheduleMessage processedMessage = MessagingScheduleMessage.builder()
                        .beginAt(Timestamp.valueOf(LocalDateTime.now()))
                        .messageId(message.getId())
                        .sendAt(message.getConfiguration().getSendAt())
                        .build();

                    switch (message.getConfiguration().getType()) {
                        case CONVERSATION:
                            try {
                                messageConversationService.send(message);
                                message.getConfiguration().setStatus(MessageStatus.SENT);
                            } catch (Exception e) {
                                processedMessage.addError(e.getMessage());
                                message.getConfiguration().setStatus(MessageStatus.ERROR);
                                log.error("Messaging scheduler processing conversation message ID: [{}] encountered an error", message.getId(), e);
                            }

                            break;
                        case EMAIL:
                            try {
                                messageEmailService.send(message);
                                message.getConfiguration().setStatus(MessageStatus.SENT);
                            } catch (Exception e) {
                                processedMessage.addError(e.getMessage());
                                message.getConfiguration().setStatus(MessageStatus.ERROR);
                                log.error("Messaging scheduler processing email message ID: [{}] encountered an error", message.getId(), e);
                            }

                            break;
                        default:
                            String error = String.format("Messaging scheduler processing email message ID: [%s] encountered an error: Invalid message type: [%s]",
                                    message.getId(),
                                    message.getConfiguration().getType());
                            log.error(error);

                            processedMessage.addError(error);
                            message.getConfiguration().setStatus(MessageStatus.ERROR);
                    }

                    messageConfigurationRepository.saveAndFlush(message.getConfiguration());
                    processedMessage.setFinishedAt(Timestamp.valueOf(LocalDateTime.now()));
                    log.info("Messaging scheduler processing message ID: [{}] complete.", message.getId());

                    return processedMessage;
                }
            )
            .toList();

        processGroupStatuses(
            messages.stream()
                .map(Message::getGroup)
                .collect(Collectors.toSet())
        );

        return Optional.of(MessagingScheduleResult.builder().processed(processed).build());
    }

    private void processGroupStatuses(Set<MessageGroup> messageGroups) {
        messageGroups.forEach(
            messageGroup -> {
                boolean isAllSent = messageGroup.getMessages().stream()
                    .allMatch(message -> message.getStatus() == MessageStatus.SENT);

                if (isAllSent) {
                    // all messages sent successfully; update group to SENT status
                    messageGroup.getConfiguration().setStatus(MessageStatus.SENT);
                    messageGroupConfigurationRepository.saveAndFlush(messageGroup.getConfiguration());

                    return;
                }

                boolean isAnyError = messageGroup.getMessages().stream()
                    .anyMatch(message -> message.getStatus() == MessageStatus.ERROR);

                if (isAnyError) {
                    // a messages send error occurred; update group to ERROR status
                    messageGroup.getConfiguration().setStatus(MessageStatus.ERROR);
                    messageGroupConfigurationRepository.saveAndFlush(messageGroup.getConfiguration());
                }
            }
        );
    }

}
