package edu.iu.terracotta.service.app.messaging.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.app.FileStorageException;
import edu.iu.terracotta.exceptions.messaging.MessageContentAttachmentException;
import edu.iu.terracotta.exceptions.messaging.MessageContentAttachmentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageContent;
import edu.iu.terracotta.model.app.messaging.MessageContentAttachment;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentAttachmentDto;
import edu.iu.terracotta.model.app.messaging.enums.MessageContentAttachmentStatus;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.repository.LtiUserRepository;
import edu.iu.terracotta.repository.messaging.MessageContentAttachmentRepository;
import edu.iu.terracotta.repository.messaging.MessageContentRepository;
import edu.iu.terracotta.service.app.messaging.MessageContentAttachmentService;
import edu.iu.terracotta.service.canvas.CanvasAPIClient;
import edu.ksu.canvas.model.Deposit;
import edu.ksu.canvas.model.File;
import edu.ksu.canvas.requestOptions.UploadOptions;

@Service
public class MessageContentAttachmentServiceImpl implements MessageContentAttachmentService {

    @Autowired private LtiUserRepository ltiUserRepository;
    @Autowired private MessageContentAttachmentRepository messageContentAttachmentRepository;
    @Autowired private MessageContentRepository messageContentRepository;
    @Autowired private CanvasAPIClient canvasAPIClient;

    @Override
    public MessageContentAttachmentDto upload(Message message, UUID uuid, MultipartFile multipartFile, SecuredInfo securedInfo) throws MessageContentNotFoundException, MessageContentNotMatchingException, MessageContentAttachmentException, CanvasApiException, IOException {
        if (!message.getContent().getUuid().equals(uuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", uuid, message.getId()));
        }

        MessageContent messageContent = messageContentRepository.findByUuid(uuid)
            .orElseThrow(() -> new MessageContentNotFoundException(String.format("No message content with UUID: [%s] found.", uuid)));

        if (!message.getContent().getUuid().equals(uuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", uuid, message.getId()));
        }

        LtiUserEntity ltiUserEntity = ltiUserRepository.findByUserKeyAndPlatformDeployment_KeyId(securedInfo.getUserId(), securedInfo.getPlatformDeploymentId());

        return doUpload(messageContent, multipartFile, ltiUserEntity);
    }

    @Override
    public MessageContentAttachmentDto delete(Message message, UUID contentUuid, UUID uuid, SecuredInfo securedInfo) throws MessageContentNotFoundException, MessageContentNotMatchingException, MessageContentAttachmentException, CanvasApiException, IOException, MessageContentAttachmentNotMatchingException {
        if (!message.getContent().getUuid().equals(contentUuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", contentUuid, message.getId()));
        }

        MessageContent messageContent = messageContentRepository.findByUuid(contentUuid)
            .orElseThrow(() -> new MessageContentNotFoundException(String.format("No message content with UUID: [%s] found.", contentUuid)));

        if (!message.getContent().getUuid().equals(contentUuid)) {
            throw new MessageContentNotMatchingException(String.format("Message content with UUID: [%s] does not match the given message with ID: [%s].", contentUuid, message.getId()));
        }

        MessageContentAttachment messageContentAttachment = messageContentAttachmentRepository.findByUuid(uuid)
            .orElseThrow(() -> new MessageContentAttachmentException(String.format("No message content attachment found with UUID: [%s]", uuid)));

        if (!message.getContent().getAttachments().stream().map(MessageContentAttachment::getId).toList().contains(messageContentAttachment.getId())) {
            throw new MessageContentAttachmentNotMatchingException(String.format("Message content attachment with UUID: [%s] does not match the given message content with ID: [%s].", uuid, messageContent.getId()));
        }

        LtiUserEntity ltiUserEntity = ltiUserRepository.findByUserKeyAndPlatformDeployment_KeyId(securedInfo.getUserId(), securedInfo.getPlatformDeploymentId());

        // delete the file in Canvas
        return delete(messageContentAttachment, ltiUserEntity);
    }

    @Override
    public MessageContentAttachmentDto delete(MessageContentAttachment messageContentAttachment, LtiUserEntity ltiUserEntity) throws CanvasApiException {
        canvasAPIClient.deleteFile(ltiUserEntity, messageContentAttachment.getCanvasId());

        // soft delete
        messageContentAttachment.setStatus(MessageContentAttachmentStatus.DELETED);

        return toDto(
            messageContentAttachmentRepository.save(messageContentAttachment)
        );
    }

    public MessageContentAttachmentDto doUpload(MessageContent messageContent, MultipartFile multipartFile, LtiUserEntity ltiUserEntity) throws MessageContentAttachmentException, CanvasApiException, IOException {
        if (multipartFile == null) {
            throw new FileStorageException("Error 140: File cannot be null.");
        }

        String filename = multipartFile.getOriginalFilename() != null ? StringUtils.cleanPath(multipartFile.getOriginalFilename()) : "";

        if (filename.contains("..")) {
            throw new FileStorageException(String.format("Error 139: filename contains invalid path sequence: '%s'", filename));
        }

        UploadOptions uploadOptions = new UploadOptions(filename);
        uploadOptions.contentType(multipartFile.getContentType());
        uploadOptions.parentFolderPath("conversation attachments");
        uploadOptions.size(multipartFile.getSize());
        uploadOptions.onDuplicateRename();
        Deposit deposit = canvasAPIClient.initializeFileUpload(ltiUserEntity, uploadOptions)
            .orElseThrow(() -> new MessageContentAttachmentException(String.format("Error initializing file upload [%s] in Canvas for user ID: [%s]", filename, ltiUserEntity.getUserId()), null));

        File file = canvasAPIClient.uploadFile(ltiUserEntity, deposit, multipartFile.getInputStream(), filename)
            .orElseThrow(() -> new MessageContentAttachmentException(String.format("Error processing file upload [%s] in Canvas for user ID: [%s]", filename, ltiUserEntity.getUserId()), null));

        return toDto(
            messageContentAttachmentRepository.save(
                MessageContentAttachment.builder()
                    .canvasId(file.getId())
                    .content(messageContent)
                    .displayName(file.getDisplayName())
                    .fileName(file.getFilename())
                    .mimeClass(file.getMimeClass())
                    .size(file.getSize())
                    .status(MessageContentAttachmentStatus.UPLOADED)
                    .url(file.getUrl())
                    .build()
            )
        );
    }

    @Override
    public MessageContentAttachment duplicate(MessageContentAttachment messageContentAttachment, MessageContent messageContent) {
        return messageContentAttachmentRepository.saveAndFlush(
            MessageContentAttachment.builder()
                .canvasId(messageContentAttachment.getCanvasId())
                .content(messageContent)
                .displayName(messageContentAttachment.getDisplayName())
                .fileName(messageContentAttachment.getFileName())
                .mimeClass(messageContentAttachment.getMimeClass())
                .size(messageContentAttachment.getSize())
                .status(messageContentAttachment.getStatus())
                .url(messageContentAttachment.getUrl())
                .build()
        );
    }

    @Override
    public List<MessageContentAttachmentDto> toDto(List<MessageContentAttachment> messageContentAttachments) {
        return CollectionUtils.emptyIfNull(messageContentAttachments).stream()
            .map(messageContentAttachment -> toDto(messageContentAttachment))
            .toList();
    }

    @Override
    public MessageContentAttachmentDto toDto(MessageContentAttachment messageContentAttachment) {
        return MessageContentAttachmentDto.builder()
            .canvasId(messageContentAttachment.getCanvasId())
            .displayName(messageContentAttachment.getDisplayName())
            .fileName(messageContentAttachment.getFileName())
            .id(messageContentAttachment.getUuid())
            .mimeClass(messageContentAttachment.getMimeClass())
            .size(messageContentAttachment.getSize())
            .status(messageContentAttachment.getStatus())
            .url(messageContentAttachment.getUrl())
            .build();
    }

}
