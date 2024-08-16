package edu.iu.terracotta.service.app.messaging.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageConversationNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageProcessingException;
import edu.iu.terracotta.exceptions.messaging.MessageSendConversationException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContentAttachment;
import edu.iu.terracotta.model.app.messaging.MessageContentStandardPlaceholder;
import edu.iu.terracotta.model.app.messaging.MessageLog;
import edu.iu.terracotta.model.app.messaging.enums.MessageContentAttachmentStatus;
import edu.iu.terracotta.model.app.messaging.enums.MessageProcessingStatus;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.repository.messaging.MessageContentStandardPlaceholderRepository;
import edu.iu.terracotta.repository.messaging.MessageLogRepository;
import edu.iu.terracotta.repository.messaging.MessageRepository;
import edu.iu.terracotta.service.app.messaging.MessageContentPlaceholderService;
import edu.iu.terracotta.service.app.messaging.MessageConversationService;
import edu.iu.terracotta.service.app.messaging.MessageSendService;
import edu.iu.terracotta.service.canvas.CanvasAPIClient;
import edu.ksu.canvas.model.Conversation;
import edu.ksu.canvas.requestOptions.CreateConversationOptions;
import edu.ksu.canvas.requestOptions.GetSingleConversationOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageConversationServiceImpl implements MessageConversationService {

    @Autowired private MessageContentStandardPlaceholderRepository messageContentStandardPlaceholderRepository;
    @Autowired private MessageLogRepository messageLogRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private CanvasAPIClient canvasAPIClient;
    @Autowired private MessageContentPlaceholderService messageContentPlaceholderService;
    @Autowired private MessageSendService messageSendService;

    @Override
    public void send(Message message) throws MessageNotMatchingException, CanvasApiException, MessageContentStandardPlaceholderNotFoundException, MessageSendConversationException {
        String canvasCourseId = messageSendService.getCanvasCourseId(message);

        LtiUserEntity ltiUserEntity = null;
        List<Conversation> conversations = null;
        String body = null;
        MessageLog messageLog = null;

        try {
            log.info("Sending conversations to Canvas for Canvas course ID: [{}] and instructor Canvas ID: [{}]", canvasCourseId, message.getOwner().getLmsUserId());
            List<MessageContentStandardPlaceholder> standardPlaceholders = messageContentStandardPlaceholderRepository.findAll();
            List<LtiUserEntity> recipients = messageSendService.getRecipients(message);

            //get all uploaded attachment IDs
            List<Long> attachmentIds = CollectionUtils.emptyIfNull(message.getContent().getAttachments()).stream()
                .filter(attachment -> attachment.getStatus() == MessageContentAttachmentStatus.UPLOADED)
                .map(MessageContentAttachment::getCanvasId)
                .toList();

            for (LtiUserEntity recipient : recipients) {
                log.info("Sending conversation message to terracotta user ID: [{}]", recipient.getUserId());
                messageLog = null;
                conversations = null;
                ltiUserEntity = recipient;

                body = messageContentPlaceholderService.process(
                    message,
                    standardPlaceholders,
                    ltiUserEntity
                );

                CreateConversationOptions createConversationOptions = new CreateConversationOptions(
                    recipient.getLmsUserId(),
                    body
                );
                createConversationOptions.attachmentIds(attachmentIds);
                createConversationOptions.forceNew(true);
                createConversationOptions.groupConversation(false);
                createConversationOptions.subject(message.getConfiguration().getSubject());

                // send to Canvas conversation API
                conversations = canvasAPIClient.sendConversation(createConversationOptions, message.getOwner());

                // add the message log
                messageLog = MessageLog.builder()
                    .body(body)
                    .canvasConversationId(conversations.get(0).getId())
                    .message(message)
                    .recipient(ltiUserEntity)
                    .status(MessageProcessingStatus.SENT)
                    .build();
                messageLogRepository.save(messageLog);
            }


            log.info("Completed sending conversations to Canvas for Canvas course ID: [{}] and instructor Canvas ID: [{}]", canvasCourseId, message.getOwner().getLmsUserId());
        } catch (Exception e) {
            log.error("Error sending conversations to Canvas for Canvas course ID: [{}] and instructor Canvas ID: [{}]", canvasCourseId, message.getOwner().getLmsUserId(), e);

            if (conversations != null) {
                if (messageLog == null) {
                    messageLog = MessageLog.builder()
                        .body(body)
                        .canvasConversationId(conversations.get(0).getId())
                        .message(message)
                        .recipient(ltiUserEntity)
                        .status(MessageProcessingStatus.ERROR)
                        .build();
                } else {
                    messageLog.setStatus(MessageProcessingStatus.ERROR);
                }

                messageLogRepository.save(messageLog);
            }

            throw new MessageSendConversationException(String.format("Error sending conversations to Canvas for Canvas course ID: [{}] and instructor Canvas ID: [{}]", canvasCourseId, message.getOwner().getLmsUserId()), e);
        }
    }

    @Override
    public Conversation get(long conditionId, UUID messageUuid, long canvasConversationId, SecuredInfo securedInfo) throws MessageNotMatchingException, CanvasApiException, MessageProcessingException, MessageConversationNotFoundException {
        Message message = messageRepository.findByUuid(messageUuid)
            .orElseThrow(() -> new MessageNotMatchingException(String.format("No message found with UUID: [%s]", messageUuid)));

        if (message.getConditionId() != conditionId) {
            throw new MessageNotMatchingException(String.format("Message with ID: [%s] does not belong to condition ID: [%s]", message.getId(), conditionId));
        }

        try {
            GetSingleConversationOptions getSingleConversationOptions = new GetSingleConversationOptions(canvasConversationId);
            getSingleConversationOptions.autoMarkAsRead(false);

            return canvasAPIClient.getConversation(getSingleConversationOptions, message.getOwner())
                .orElseThrow(() -> new MessageConversationNotFoundException(String.format("No Canvas conversation with ID: [%s] found.", canvasConversationId)));
        } catch (CanvasApiException e) {
            log.error(String.format("Error retrieving Canvas conversation ID: [%s] for Canvas user ID: [%s]", canvasConversationId, message.getOwner().getLmsUserId()), e);
            throw new MessageProcessingException(String.format("Error retrieving Canvas conversation ID: [%s] for Canvas user ID: [%s]", canvasConversationId, message.getOwner().getLmsUserId()), e);
        }
    }

    @Override
    public List<Conversation> getAllForUser(long treatmentId, SecuredInfo securedInfo) throws CanvasApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllForUser'");
    }

    @Override
    public List<Conversation> getAllForMessage(long treatmentId, UUID messageUuid, SecuredInfo securedInfo) throws CanvasApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllForMessage'");
    }

}
