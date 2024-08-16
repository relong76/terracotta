package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.model.app.messaging.MessageConfiguration;
import edu.iu.terracotta.model.app.messaging.MessageEmailReplyTo;
import edu.iu.terracotta.model.app.messaging.dto.MessageEmailReplyToDto;
import edu.iu.terracotta.repository.messaging.MessageEmailReplyToRepository;
import edu.iu.terracotta.service.app.messaging.MessageEmailReplyToService;

@Service
public class MessageEmailReplyToServiceImpl implements MessageEmailReplyToService {

    @Autowired private MessageEmailReplyToRepository messageEmailReplyToRepository;

    @Override
    public MessageEmailReplyTo duplicate(MessageEmailReplyTo messageEmailReplyTo, MessageConfiguration messageConfiguration) {
        return messageEmailReplyToRepository.saveAndFlush(
            MessageEmailReplyTo.builder()
                .configuration(messageConfiguration)
                .email(messageEmailReplyTo.getEmail())
                .build()
        );
    }

    @Override
    public List<MessageEmailReplyToDto> toDto(List<MessageEmailReplyTo> messageEmailReplyTos) {
        return CollectionUtils.emptyIfNull(messageEmailReplyTos).stream()
            .map(messageEmailReplyTo -> toDto(messageEmailReplyTo))
            .toList();
    }

    @Override
    public MessageEmailReplyToDto toDto(MessageEmailReplyTo messageEmailReplyTo) {
        if (messageEmailReplyTo == null) {
            return null;
        }

        return MessageEmailReplyToDto.builder()
            .configurationId(messageEmailReplyTo.getConfiguration().getUuid())
            .email(messageEmailReplyTo.getEmail())
            .id(messageEmailReplyTo.getUuid())
            .build();
    }

}
