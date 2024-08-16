package edu.iu.terracotta.service.app.messaging;

import java.util.List;

import edu.iu.terracotta.model.app.messaging.MessageConfiguration;
import edu.iu.terracotta.model.app.messaging.MessageEmailReplyTo;
import edu.iu.terracotta.model.app.messaging.dto.MessageEmailReplyToDto;

public interface MessageEmailReplyToService {

    MessageEmailReplyTo duplicate(MessageEmailReplyTo messageEmailReplyTo, MessageConfiguration messageConfiguration);
    List<MessageEmailReplyToDto> toDto(List<MessageEmailReplyTo> messageEmailReplyTos);
    MessageEmailReplyToDto toDto(MessageEmailReplyTo messageEmailReplyTo);

}
