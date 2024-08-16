package edu.iu.terracotta.service.app.messaging;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageSendEmailException;
import edu.iu.terracotta.model.app.messaging.Message;

public interface MessageEmailService {

    void send(Message message) throws MessageNotMatchingException, CanvasApiException, MessageContentStandardPlaceholderNotFoundException, MessageSendEmailException;

}
