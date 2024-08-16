
package edu.iu.terracotta.service.app.messaging;

import java.util.List;

import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.MessageGroupConfiguration;
import edu.iu.terracotta.model.app.messaging.dto.MessageGroupConfigurationDto;

public interface MessageGroupConfigurationService {

    MessageGroupConfiguration create(MessageGroup messageGroup);
    MessageGroupConfigurationDto update(MessageGroupConfigurationDto messageGroupConfigurationDto, MessageGroupConfiguration messageGroupConfiguration);
    List<MessageGroupConfigurationDto> updateAll(List<MessageGroupConfigurationDto> messageGroupConfigurationDtos, List<MessageGroupConfiguration> messageGroupConfigurations);
    MessageGroupConfiguration duplicate(MessageGroupConfiguration messageGroupConfiguration, MessageGroup messageGroup);
    MessageGroupConfigurationDto toDto(MessageGroupConfiguration messageGroupConfiguration);
    MessageGroupConfiguration fromDto(MessageGroupConfigurationDto messageGroupConfigurationDto, MessageGroupConfiguration messageGroupConfiguration);

}
