package edu.iu.terracotta.service.app.messaging.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.repository.messaging.MessageContentRepository;
import edu.iu.terracotta.repository.messaging.MessageContentStandardPlaceholderRepository;
import edu.iu.terracotta.repository.messaging.MessageRepository;
import edu.iu.terracotta.service.app.messaging.MessageContentAttachmentService;
import edu.iu.terracotta.service.app.messaging.MessageContentCustomPlaceholderService;
import edu.iu.terracotta.service.app.messaging.MessageContentService;
import edu.iu.terracotta.service.app.messaging.MessageContentStandardPlaceholderService;
import edu.iu.terracotta.service.app.messaging.MessageRuleService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageContentServiceImpl implements MessageContentService {

    @Autowired private MessageContentRepository messageContentRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private MessageContentStandardPlaceholderRepository messageContentStandardPlaceholderRepository;
    @Autowired private MessageContentAttachmentService messageContentAttachmentService;
    @Autowired private MessageContentCustomPlaceholderService messageContentCustomPlaceholderService;
    @Autowired private MessageContentStandardPlaceholderService messageContentStandardPlaceholderService;
    @Autowired private MessageRuleService messageRuleService;

    @Override
    public MessageContentDto findOne(MessageContent messageContent) {
        return toDto(messageContent);
    }

    @Override
    public MessageContentDto create(Message message) {
        MessageContent messageContent = messageContentRepository.save(
            MessageContent.builder()
                .message(message)
                .build()
        );

        message.setContent(messageContent);
        messageRepository.save(message);

        return toDto(messageContent);
    }

    @Override
    public MessageContentDto update(MessageContentDto messageContentDto, MessageContent messageContent, SecuredInfo securedInfo) {
        messageContentRepository.save(
            fromDto(
                messageContentDto,
                messageContent
            )
        );

        messageContentAttachmentService.duplicateDtos(messageContentDto.getAttachments(), messageContent, securedInfo);

        return toDto(messageContentRepository.findById(messageContent.getId()).get());
    }

    @Override
    public MessageContent duplicate(MessageContent messageContent, Message message, SecuredInfo securedInfo) {
        MessageContent newMessageContent = messageContentRepository.saveAndFlush(
            MessageContent.builder()
                .body(messageContent.getBody())
                .message(message)
                .build()
        );

        messageContentAttachmentService.duplicate(messageContent.getAttachments(), newMessageContent, securedInfo);

        // TODO handle rules and custom placeholder duplication
        /*newMessageContent.setRules(
            messageContent.getRules().stream()
                .map(rule -> messageRuleService.duplicate(rule, newMessageContent))
                .toList()
        );

        newMessageContent.setCustomPlaceholders(
            messageContent.getCustomPlaceholders().stream()
                .map(customPlaceholder -> messageContentCustomPlaceholderService.duplicate(customPlaceholder, newMessageContent))
                .toList()
        );*/

        return messageContentRepository.saveAndFlush(newMessageContent);
    }

    @Override
    public MessageContentDto toDto(MessageContent messageContent) {
        if (messageContent == null) {
            return null;
        }

        return MessageContentDto.builder()
            .id(messageContent.getUuid())
            .messageId(messageContent.getMessage().getUuid())
            .body(messageContent.getBody())
            .customPlaceholders(messageContentCustomPlaceholderService.toDto(messageContent.getCustomPlaceholders(), messageContent.getMessage().getUuid()))
            .standardPlaceholders(messageContentStandardPlaceholderService.toDto(messageContentStandardPlaceholderRepository.findAllByEnabled(true)))
            .rules(messageRuleService.toDto(messageContent.getRules(), messageContent.getUuid()))
            .attachments(messageContentAttachmentService.toDto(messageContent.getAttachments()))
            .build();
    }

    @Override
    public MessageContent fromDto(MessageContentDto messageContentDto, MessageContent messageContent) {
        messageContent.setBody(messageContentDto.getBody());

        return messageContent;
    }

}
