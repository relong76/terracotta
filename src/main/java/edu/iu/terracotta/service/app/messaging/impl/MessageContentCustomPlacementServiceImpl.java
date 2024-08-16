package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.messaging.MessageContentCustomPlaceholderNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentCustomPlaceholderNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageNotFoundException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.MessageContentCustomPlaceholder;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentCustomPlaceholderDto;
import edu.iu.terracotta.repository.messaging.MessageContentCustomPlaceholderRepository;
import edu.iu.terracotta.repository.messaging.MessageContentRepository;
import edu.iu.terracotta.repository.messaging.MessageRepository;
import edu.iu.terracotta.service.app.messaging.MessageContentCustomPlaceholderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageContentCustomPlacementServiceImpl implements MessageContentCustomPlaceholderService {

    @Autowired private MessageContentCustomPlaceholderRepository messageContentCustomPlaceholderRepository;
    @Autowired private MessageContentRepository messageContentRepository;
    @Autowired private MessageRepository messageRepository;

    @Override
    public MessageContentCustomPlaceholderDto create(MessageContentCustomPlaceholderDto messageContentCustomPlaceholderDto, UUID messageUuid, UUID contentUuid) throws MessageNotFoundException, MessageContentNotFoundException, MessageContentNotMatchingException {
        Message message = messageRepository.findByUuid(messageUuid)
            .orElseThrow(() -> new MessageNotFoundException(String.format("No message with UUID: [%s] found.", messageUuid)));

        if (!message.getContent().getUuid().equals(contentUuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", contentUuid, message.getId()));
        }

        MessageContent messageContent = messageContentRepository.findByUuid(contentUuid)
            .orElseThrow(() -> new MessageContentNotFoundException(String.format("No message content with UUID: [%s] found.", contentUuid)));

        MessageContentCustomPlaceholder messageContentCustomPlaceholder = MessageContentCustomPlaceholder.builder()
            .content(messageContent)
            .build();

        return toDto(
            messageContentCustomPlaceholderRepository.save(
                fromDto(messageContentCustomPlaceholderDto, messageContentCustomPlaceholder)
            ),
            contentUuid
        );
    }

    @Override
    public MessageContentCustomPlaceholderDto update(MessageContentCustomPlaceholderDto messageContentCustomPlaceholderDto, UUID messageUuid, UUID contentUuid, UUID uuid)
            throws MessageNotFoundException, MessageContentNotFoundException, MessageContentNotMatchingException, MessageContentCustomPlaceholderNotFoundException, MessageContentCustomPlaceholderNotMatchingException {
        Message message = messageRepository.findByUuid(messageUuid)
            .orElseThrow(() -> new MessageNotFoundException(String.format("No message with UUID: [%s] found.", messageUuid)));

        if (!message.getContent().getUuid().equals(contentUuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", contentUuid, message.getId()));
        }

        MessageContent messageContent = messageContentRepository.findByUuid(contentUuid)
            .orElseThrow(() -> new MessageContentNotFoundException(String.format("No message content with UUID: [%s] found.", contentUuid)));

        boolean hasPlaceholder = messageContent.getCustomPlaceholders().stream()
            .anyMatch(customPlaceholder -> customPlaceholder.getUuid().equals(uuid));

        if (!hasPlaceholder) {
            throw new MessageContentCustomPlaceholderNotMatchingException(String.format("Message content custom placeholder UUID: [%s] is not associated with message content with ID: [%s]", uuid, messageContent.getId()));
        }

        return toDto(
            fromDto(messageContentCustomPlaceholderDto,
                messageContentCustomPlaceholderRepository.findByUuid(uuid)
                    .orElseThrow(() -> new MessageContentCustomPlaceholderNotFoundException(String.format("No message content custom placeholder with UUID: [%s] found.", uuid)))
            ),
            contentUuid
        );
    }

    @Override
    public void delete(UUID messageUuid, UUID contentUuid, UUID uuid) {
        Optional<Message> message = messageRepository.findByUuid(messageUuid);

        if (message.isEmpty()) {
            log.error("No message with UUID: [{}] found.", messageUuid);
            return;
        }

        if (!message.get().getContent().getUuid().equals(contentUuid)) {
            log.error("Message content with UUID: [%s] does not match the given message with ID: [{}].", contentUuid, message.get().getId());
            return;
        }

        Optional<MessageContent> messageContent = messageContentRepository.findByUuid(contentUuid);

        if (messageContent.isEmpty()) {
            log.error("No message content with UUID: [{}] found.", contentUuid);
            return;
        }

        boolean hasPlaceholder = messageContent.get().getCustomPlaceholders().stream()
            .anyMatch(customPlaceholder -> customPlaceholder.getUuid().equals(uuid));

        if (!hasPlaceholder) {
            log.error("Message content custom placeholder UUID: [{}] is not associated with message content with ID: [{}]", uuid, messageContent.get().getId());
            return;
        }

        Optional<MessageContentCustomPlaceholder> messageContentCustomPlaceholder = messageContentCustomPlaceholderRepository.findByUuid(uuid);

        if (messageContentCustomPlaceholder.isEmpty()) {
            log.error("No message content custom placeholder with UUID: [{}] found.", uuid);
            return;
        }

        messageContentCustomPlaceholderRepository.deleteById(messageContentCustomPlaceholder.get().getId());
    }

    @Override
    public MessageContentCustomPlaceholder duplicate(MessageContentCustomPlaceholder messageContentCustomPlaceholder, MessageContent messageContent) {
        return messageContentCustomPlaceholderRepository.saveAndFlush(
            MessageContentCustomPlaceholder.builder()
                .content(messageContent)
                .enabled(messageContentCustomPlaceholder.isEnabled())
                .key(messageContentCustomPlaceholder.getKey())
                .label(messageContentCustomPlaceholder.getLabel())
                .build()
        );
    }

    @Override
    public List<MessageContentCustomPlaceholderDto> toDto(List<MessageContentCustomPlaceholder> messageContentCustomPlaceholders, UUID messageContentUuid) {
        return CollectionUtils.emptyIfNull(messageContentCustomPlaceholders).stream()
            .map(customPlaceholder -> toDto(customPlaceholder, messageContentUuid))
            .toList();
    }

    @Override
    public MessageContentCustomPlaceholderDto toDto(MessageContentCustomPlaceholder messageContentCustomPlaceholder, UUID messageContentUuid) {
        if (messageContentCustomPlaceholder == null) {
            return null;
        }

        return MessageContentCustomPlaceholderDto.builder()
            .id(messageContentCustomPlaceholder.getUuid())
            .contentId(messageContentUuid.toString())
            .label(messageContentCustomPlaceholder.getLabel())
            .key(messageContentCustomPlaceholder.getKey())
            .enabled(messageContentCustomPlaceholder.isEnabled())
            .build();
    }

    @Override
    public MessageContentCustomPlaceholder fromDto(MessageContentCustomPlaceholderDto messageContentCustomPlaceholderDto, MessageContentCustomPlaceholder messageContentCustomPlaceholder) {
        messageContentCustomPlaceholder.setLabel(messageContentCustomPlaceholderDto.getLabel());

        return messageContentCustomPlaceholder;
    }

}
