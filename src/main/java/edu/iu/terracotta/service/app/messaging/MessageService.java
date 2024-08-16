package edu.iu.terracotta.service.app.messaging;

import java.util.List;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.dto.MessageDto;

public interface MessageService {

    MessageDto update(MessageDto messageDto, long exposureId, MessageGroup groupMessage, Message message);
    Message duplicate(Message message, MessageGroup messageGroup);
    MessageDto copy(Message fromMessage, Message toMessage);
    List<MessageDto> toDto(List<Message> messages);
    MessageDto toDto(Message message);

}
