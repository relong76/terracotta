package edu.iu.terracotta.service.app.messaging;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentDto;

public interface MessageContentService {

    MessageContentDto findOne(MessageContent messageContent);
    MessageContentDto create(Message message);
    MessageContentDto update(MessageContentDto messageContentDto, MessageContent messageContent);
    MessageContent duplicate(MessageContent messageContent);
    MessageContentDto toDto(MessageContent messageContent);
    MessageContent fromDto(MessageContentDto messageContentDto, MessageContent messageContent);

}
