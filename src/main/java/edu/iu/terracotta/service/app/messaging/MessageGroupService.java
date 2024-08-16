package edu.iu.terracotta.service.app.messaging;

import java.util.List;
import edu.iu.terracotta.model.app.Exposure;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageGroupDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;

public interface MessageGroupService {

    MessageGroupDto get(MessageGroup messageGroup);
    List<MessageGroupDto> getAll(long experimentId, long exposureId, SecuredInfo securedInfo);
    MessageGroupDto create(Exposure exposure, boolean single, SecuredInfo securedInfo);
    MessageGroupDto update(MessageGroupDto messageGroupDto, MessageGroup messageGroup);
    MessageGroupDto send(MessageGroup messageGroup);
    MessageGroupDto delete(MessageGroup messageGroup);
    MessageGroupDto move(Exposure exposure, MessageGroup messageGroup);
    MessageGroupDto duplicate(MessageGroup messageGroup, Exposure exposure, SecuredInfo securedInfo);
    List<MessageGroupDto> toDto(List<MessageGroup> messageGroups);
    MessageGroupDto toDto(MessageGroup messageGroup);

}
