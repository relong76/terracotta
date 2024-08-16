package edu.iu.terracotta.service.app.messaging;

import java.util.List;

import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContentStandardPlaceholder;

public interface MessageContentPlaceholderService {

    String process(Message message, List<MessageContentStandardPlaceholder> standardPlaceholders, LtiUserEntity ltiUserEntity) throws MessageContentStandardPlaceholderNotFoundException;

}
