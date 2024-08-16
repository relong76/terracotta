package edu.iu.terracotta.service.app.messaging;

import java.util.List;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;

public interface MessageSendService {

    List<LtiUserEntity> getRecipients(Message message) throws CanvasApiException;
    String getCanvasCourseId(Message message);

}
