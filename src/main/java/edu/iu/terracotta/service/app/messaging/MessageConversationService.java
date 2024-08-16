package edu.iu.terracotta.service.app.messaging;

import java.util.List;
import java.util.UUID;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageConversationNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageProcessingException;
import edu.iu.terracotta.exceptions.messaging.MessageSendConversationException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.ksu.canvas.model.Conversation;

public interface MessageConversationService {

    void send(Message message) throws MessageNotMatchingException, CanvasApiException, MessageContentStandardPlaceholderNotFoundException, MessageSendConversationException;
    Conversation get(long conditionId, UUID messageUuid, long canvasConversationId, SecuredInfo securedInfo) throws MessageNotMatchingException, CanvasApiException, MessageProcessingException, MessageConversationNotFoundException;
    List<Conversation> getAllForUser(long conditionId, SecuredInfo securedInfo) throws CanvasApiException;
    List<Conversation> getAllForMessage(long conditionId, UUID messageUuid, SecuredInfo securedInfo) throws CanvasApiException;

}
