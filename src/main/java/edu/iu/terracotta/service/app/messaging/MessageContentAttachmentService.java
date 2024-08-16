package edu.iu.terracotta.service.app.messaging;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.messaging.MessageContentAttachmentException;
import edu.iu.terracotta.exceptions.messaging.MessageContentAttachmentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.MessageContentAttachment;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentAttachmentDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;

public interface MessageContentAttachmentService {

    MessageContentAttachmentDto upload(Message message, UUID uuid, MultipartFile multipartFile, SecuredInfo securedInfo) throws MessageContentNotFoundException, MessageContentNotMatchingException, MessageContentAttachmentException, CanvasApiException, IOException;
    void delete(Message message, UUID contentUuid, UUID uuid, SecuredInfo securedInfo) throws MessageContentNotFoundException, MessageContentNotMatchingException, MessageContentAttachmentException, CanvasApiException, IOException, MessageContentAttachmentNotMatchingException;
    void delete(MessageContentAttachment messageContentAttachment, LtiUserEntity ltiUserEntity) throws CanvasApiException;
    List<MessageContentAttachment> duplicateDtos(List<MessageContentAttachmentDto> messageContentAttachmentDtos, MessageContent messageContent, SecuredInfo securedInfo);
    List<MessageContentAttachment> duplicate(List<MessageContentAttachment> messageContentAttachments, MessageContent messageContent, SecuredInfo securedInfo);
    List<MessageContentAttachmentDto> toDto(List<MessageContentAttachment> messageContentAttachments);
    MessageContentAttachmentDto toDto(MessageContentAttachment messageContentAttachment);

}
