package edu.iu.terracotta.service.app.messaging;

import java.util.UUID;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageConfiguration;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageConfigurationDto;

public interface MessageConfigurationService {

    MessageConfigurationDto update(MessageConfigurationDto messageConfigurationDto, MessageGroup messageGroup, Message message, MessageConfiguration messageConfiguration);
    MessageConfiguration duplicate(MessageConfiguration messageConfiguration, Message message);
    MessageConfigurationDto toDto(MessageConfiguration messageConfiguration, UUID messageUuid);
    MessageConfiguration fromDto(MessageConfigurationDto messageConfigurationDto, MessageConfiguration messageConfiguration);

}
