package edu.iu.terracotta.service.app.messaging;

import java.util.List;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.dto.MessageDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;

public interface MessageService {

    MessageDto get(Message message);
    MessageDto update(MessageDto messageDto, long exposureId, MessageGroup groupMessage, Message message);
    Message duplicate(Message message, MessageGroup messageGroup, SecuredInfo securedInfo);
    List<MessageDto> toDto(List<Message> messages);
    MessageDto toDto(Message message);

}
