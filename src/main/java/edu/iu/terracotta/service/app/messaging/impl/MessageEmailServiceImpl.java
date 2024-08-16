package edu.iu.terracotta.service.app.messaging.impl;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.messaging.MessageContentStandardPlaceholderNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageSendEmailException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContentAttachment;
import edu.iu.terracotta.model.app.messaging.MessageContentStandardPlaceholder;
import edu.iu.terracotta.model.app.messaging.MessageLog;
import edu.iu.terracotta.model.app.messaging.enums.MessageContentAttachmentStatus;
import edu.iu.terracotta.model.app.messaging.enums.MessageProcessingStatus;
import edu.iu.terracotta.repository.messaging.MessageContentStandardPlaceholderRepository;
import edu.iu.terracotta.repository.messaging.MessageLogRepository;
import edu.iu.terracotta.service.app.messaging.MessageContentAttachmentService;
import edu.iu.terracotta.service.app.messaging.MessageContentPlaceholderService;
import edu.iu.terracotta.service.app.messaging.MessageEmailService;
import edu.iu.terracotta.service.app.messaging.MessageSendService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageEmailServiceImpl implements MessageEmailService {

    @Autowired private MessageContentStandardPlaceholderRepository messageContentStandardPlaceholderRepository;
    @Autowired private MessageContentAttachmentService messageContentAttachmentService;
    @Autowired private MessageContentPlaceholderService messageContentPlaceholderService;
    @Autowired private MessageLogRepository messageLogRepository;
    @Autowired private JavaMailSender javaMailSender;
    @Autowired private MessageSendService messageSendService;

    @Value("${app.messaging.email.from:no-reply@terracotta.education}")
    private String from;

    @Override
    public void send(Message message) throws MessageNotMatchingException, MessageContentStandardPlaceholderNotFoundException, MessageSendEmailException {
        String canvasCourseId = messageSendService.getCanvasCourseId(message);

        LtiUserEntity ltiUserEntity = null;
        MimeMessageHelper emailMessage = null;
        String body = null;
        MessageLog messageLog = null;

        try {
            log.info("Sending emails for message ID: [{}]", message.getId());
            List<MessageContentStandardPlaceholder> standardPlaceholders = messageContentStandardPlaceholderRepository.findAll();
            List<LtiUserEntity> recipients = messageSendService.getRecipients(message);

            emailMessage = new MimeMessageHelper(
                javaMailSender.createMimeMessage(),
                true
            );
            emailMessage.setFrom(from);
            emailMessage.setSubject(message.getConfiguration().getSubject());
            emailMessage.getMimeMessage().setReplyTo(
                CollectionUtils.emptyIfNull(message.getConfiguration().getReplyTo()).stream()
                    .map(
                        replyTo -> {
                            try {
                                return new InternetAddress(replyTo.getEmail());
                            } catch (AddressException e) {
                                log.error("Error converting reply-to address: [{}]", replyTo.getEmail(), e);
                                return null;
                            }
                        }
                    )
                    .filter(Objects::nonNull)
                    .toArray(replyTo -> new InternetAddress[replyTo])
            );
            // add any uploaded attachments to the email (stored in Canvas)
            List<MessageContentAttachment> availableAttachments = message.getContent().getAttachments().stream()
                .filter(attachment -> attachment.getStatus() == MessageContentAttachmentStatus.UPLOADED)
                .toList();

            List<Map<String, ByteArrayResource>> attachments = new ArrayList<>();

            for (MessageContentAttachment attachment : availableAttachments) {
                try (InputStream inputStream = URI.create(attachment.getUrl()).toURL().openStream()) {
                    attachments.add(
                        Collections.singletonMap(
                            attachment.getFileName(),
                            new ByteArrayResource(
                                IOUtils.toByteArray(inputStream)
                            )
                        )
                    );
                    inputStream.close();
                } catch (Exception e) {
                    log.error("Error retrieving file ID: [{}] from Canvas for message with ID: [{}]", attachment.getId(), message.getId(), e);
                }
            }

            for (Map<String, ByteArrayResource> attachment : attachments) {
                for (Map.Entry<String, ByteArrayResource> a : attachment.entrySet()) {
                    try {
                        emailMessage.addAttachment(a.getKey(), a.getValue());
                    } catch (Exception e) {
                        log.error("Error attaching file to email message with ID: [{}]", message.getId(), e);
                    }
                }
            }

            for (LtiUserEntity recipient : recipients) {
                log.info("Sending email message to terracotta user ID: [{}]", recipient.getUserId());
                messageLog = null;
                ltiUserEntity = recipient;

                emailMessage.setTo(recipient.getEmail());

                body = messageContentPlaceholderService.process(
                    message,
                    standardPlaceholders,
                    ltiUserEntity
                );

                emailMessage.setText(
                    body,
                    true
                );

                javaMailSender.send(emailMessage.getMimeMessage());

                // add the message log
                messageLog = MessageLog.builder()
                    .body(body)
                    .message(message)
                    .recipient(ltiUserEntity)
                    .status(MessageProcessingStatus.SENT)
                    .build();
                messageLogRepository.save(messageLog);
            }

            // delete attachment files from Canvas
            availableAttachments.forEach(
                attachment -> {
                    try {
                        messageContentAttachmentService.delete(attachment, message.getOwner());
                    } catch (CanvasApiException e) {
                        log.error("Error deleting attachment file with Canvas ID: [{}] in Canvas.", attachment.getCanvasId(), e);
                    }
                }
            );

            log.info("Completed sending emails for message ID: [{}]", message.getId());
        } catch (Exception e) {
            log.error("Error sending email for message ID: [{}] to recipient user ID: [{}] ", message.getId(), ltiUserEntity.getUserId(), e);

            if (emailMessage != null) {
                if (messageLog == null) {
                    messageLog = MessageLog.builder()
                        .body(body)
                        .message(message)
                        .recipient(ltiUserEntity)
                        .status(MessageProcessingStatus.ERROR)
                        .build();
                } else {
                    messageLog.setStatus(MessageProcessingStatus.ERROR);
                }

                messageLogRepository.save(messageLog);
            }

            throw new MessageSendEmailException(String.format("Error sending email for Canvas course ID: [{}] and instructor Canvas ID: [{}]", canvasCourseId, message.getOwner().getLmsUserId()), e);
        }
    }

}
