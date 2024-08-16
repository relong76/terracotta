package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.model.app.messaging.MessageContentStandardPlaceholder;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentStandardPlaceholderDto;
import edu.iu.terracotta.repository.messaging.MessageContentStandardPlaceholderRepository;
import edu.iu.terracotta.service.app.messaging.MessageContentStandardPlaceholderService;

@Service
public class MessageContentStandardPlacementServiceImpl implements MessageContentStandardPlaceholderService {

    @Autowired private MessageContentStandardPlaceholderRepository messageContentStandardPlaceholderRepository;

    @Override
    public MessageContentStandardPlaceholderDto findOne(UUID uuid) throws MessageContentStandardPlaceholderNotFoundException {
        return toDto(
            messageContentStandardPlaceholderRepository.findByUuid(uuid)
                .orElseThrow(() -> new MessageContentStandardPlaceholderNotFoundException(String.format("No message content standard placeholder with UUID: [%s] found.", uuid)))
        );
    }

    @Override
    public List<MessageContentStandardPlaceholderDto> toDto(List<MessageContentStandardPlaceholder> messageContentStandardPlaceholders) {
        return CollectionUtils.emptyIfNull(messageContentStandardPlaceholders).stream()
            .map(standardPlaceholder -> toDto(standardPlaceholder))
            .toList();
    }

    @Override
    public MessageContentStandardPlaceholderDto toDto(MessageContentStandardPlaceholder messageContentStandardPlaceholder) {
        if (messageContentStandardPlaceholder == null) {
            return null;
        }

        return MessageContentStandardPlaceholderDto.builder()
            .id(messageContentStandardPlaceholder.getUuid())
            .label(messageContentStandardPlaceholder.getLabel())
            .key(messageContentStandardPlaceholder.getKey())
            .enabled(messageContentStandardPlaceholder.isEnabled())
            .build();
    }

}
