package edu.iu.terracotta.service.app.messaging;

import java.util.List;
import java.util.UUID;

import edu.iu.terracotta.exceptions.messaging.MessageContentCustomPlaceholderNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentCustomPlaceholderNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageNotFoundException;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.MessageContentCustomPlaceholder;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentCustomPlaceholderDto;

public interface MessageContentCustomPlaceholderService {

    MessageContentCustomPlaceholderDto create(MessageContentCustomPlaceholderDto messageContentCustomPlaceholderDto, UUID messageUuid, UUID contentUuid) throws MessageNotFoundException, MessageContentNotFoundException, MessageContentNotMatchingException;
    MessageContentCustomPlaceholderDto update(MessageContentCustomPlaceholderDto messageContentCustomPlaceholderDto, UUID messageUuid, UUID contentUuid, UUID uuid) throws MessageNotFoundException, MessageContentNotFoundException, MessageContentNotMatchingException, MessageContentCustomPlaceholderNotFoundException, MessageContentCustomPlaceholderNotMatchingException;
    void delete(UUID messageUuid, UUID contentUuid, UUID uuid);
    MessageContentCustomPlaceholder duplicate(MessageContentCustomPlaceholder messageContentCustomPlaceholder, MessageContent messageContent);
    List<MessageContentCustomPlaceholderDto> toDto(List<MessageContentCustomPlaceholder> messageContentCustomPlaceholders, UUID messageContentUuid);
    MessageContentCustomPlaceholderDto toDto(MessageContentCustomPlaceholder messageContentCustomPlaceholder, UUID messageContentUuid);
    MessageContentCustomPlaceholder fromDto(MessageContentCustomPlaceholderDto messageContentCustomPlaceholderDto, MessageContentCustomPlaceholder messageContentCustomPlaceholder);

}
