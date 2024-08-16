package edu.iu.terracotta.service.app.messaging;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;

public interface MessageContentService {

    MessageContentDto findOne(MessageContent messageContent);
    MessageContentDto create(Message message);
    MessageContentDto update(MessageContentDto messageContentDto, MessageContent messageContent, SecuredInfo securedInfo);
    MessageContent duplicate(MessageContent messageContent, Message message, SecuredInfo securedInfo);
    MessageContentDto toDto(MessageContent messageContent);
    MessageContent fromDto(MessageContentDto messageContentDto, MessageContent messageContent);

}
