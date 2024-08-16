package edu.iu.terracotta.service.app.messaging;

import java.util.List;
import java.util.UUID;

import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.model.app.messaging.MessageContentStandardPlaceholder;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentStandardPlaceholderDto;

public interface MessageContentStandardPlaceholderService {

    MessageContentStandardPlaceholderDto findOne(UUID uuid) throws MessageContentStandardPlaceholderNotFoundException;
    List<MessageContentStandardPlaceholderDto> toDto(List<MessageContentStandardPlaceholder> messageContentStandardPlaceholders);
    MessageContentStandardPlaceholderDto toDto(MessageContentStandardPlaceholder messageContentStandardPlaceholder);

}
