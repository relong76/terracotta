package edu.iu.terracotta.service.app.messaging.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
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
    public void delete(Message message, UUID contentUuid, UUID uuid, SecuredInfo securedInfo) throws MessageContentNotFoundException, MessageContentNotMatchingException, MessageContentAttachmentException, CanvasApiException, IOException, MessageContentAttachmentNotMatchingException {
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
        Thread thread = new Thread(
            () -> {
                try {
                    delete(messageContentAttachment, ltiUserEntity);
                } catch (CanvasApiException e) {
                    log.error("Error deleting file ID: [{}] in Canvas.", messageContentAttachment.getId(), e);
                    messageContentAttachment.setStatus(MessageContentAttachmentStatus.ERROR);
                    messageContentAttachmentRepository.save(messageContentAttachment);
                }
            }
        );

        thread.start();
    }

    @Override
    public void delete(MessageContentAttachment messageContentAttachment, LtiUserEntity ltiUserEntity) throws CanvasApiException {
        canvasAPIClient.deleteFile(ltiUserEntity, messageContentAttachment.getCanvasId());

        // soft delete
        messageContentAttachment.setStatus(MessageContentAttachmentStatus.DELETED);

        messageContentAttachmentRepository.save(messageContentAttachment);
    }

    public MessageContentAttachmentDto doUpload(MessageContent messageContent, MultipartFile multipartFile, LtiUserEntity ltiUserEntity) throws MessageContentAttachmentException, CanvasApiException, IOException {
        if (multipartFile == null) {
            throw new FileStorageException("Error 140: File cannot be null.");
        }

        String filename = multipartFile.getOriginalFilename() != null ? StringUtils.cleanPath(multipartFile.getOriginalFilename()) : "";

        if (filename.contains("..")) {
            throw new FileStorageException(String.format("Error 139: filename contains invalid path sequence: [%s]", filename));
        }

        MessageContentAttachment newMessageContentAttachment = messageContentAttachmentRepository.saveAndFlush(
            MessageContentAttachment.builder()
                .content(messageContent)
                .displayName(multipartFile.getOriginalFilename())
                .fileName(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .size(multipartFile.getSize())
                .status(MessageContentAttachmentStatus.UPLOADED)
                .build()
        );

        InputStream fileInputStream = multipartFile.getInputStream();

        Thread thread = new Thread(
            () ->
                {
                    try {
                        UploadOptions uploadOptions = new UploadOptions(filename);
                        uploadOptions.contentType(multipartFile.getContentType());
                        uploadOptions.parentFolderPath("conversation attachments");
                        uploadOptions.size(multipartFile.getSize());
                        uploadOptions.onDuplicateRename();
                        Deposit deposit = canvasAPIClient.initializeFileUpload(ltiUserEntity, uploadOptions)
                            .orElseThrow(() -> new MessageContentAttachmentException(String.format("Error initializing file upload [%s] in Canvas for user ID: [%s]", filename, ltiUserEntity.getUserId()), null));

                        File file = canvasAPIClient.uploadFile(ltiUserEntity, deposit, fileInputStream, filename)
                            .orElseThrow(() -> new MessageContentAttachmentException(String.format("Error processing file upload [%s] in Canvas for user ID: [%s]", filename, ltiUserEntity.getUserId()), null));

                        newMessageContentAttachment.setCanvasId(file.getId());
                        newMessageContentAttachment.setDisplayName(file.getDisplayName());
                        newMessageContentAttachment.setFileName(file.getFilename());
                        newMessageContentAttachment.setSize(file.getSize());
                        newMessageContentAttachment.setUrl(file.getUrl());
                        messageContentAttachmentRepository.save(newMessageContentAttachment);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                }
        );

        thread.start();

        return toDto(newMessageContentAttachment);
    }

    @Override
    public List<MessageContentAttachment> duplicateDtos(List<MessageContentAttachmentDto> messageContentAttachmentDtos, MessageContent messageContent, SecuredInfo securedInfo) {
        return duplicate(
            CollectionUtils.emptyIfNull(messageContentAttachmentDtos).stream()
                .map(
                    attachmentDto -> {
                        try {
                            return messageContentAttachmentRepository.findByUuid(attachmentDto.getId())
                                .orElseThrow(() -> new MessageContentAttachmentNotMatchingException(String.format("No attachment with UUID: [%s] found.", attachmentDto.getId())));
                        } catch (MessageContentAttachmentNotMatchingException e) {
                            log.error(e.getMessage(), e);
                            return null;
                        }
                    }
                )
                .filter(Objects::nonNull)
                .toList(),
            messageContent,
            securedInfo
        );
    }

    @Override
    public List<MessageContentAttachment> duplicate(List<MessageContentAttachment> messageContentAttachments, MessageContent messageContent, SecuredInfo securedInfo) {
        List<UUID> currentAttachmentUuids = CollectionUtils.emptyIfNull(messageContent.getAttachments()).stream()
            .filter(messageContentAttachment -> messageContentAttachment.getStatus() == MessageContentAttachmentStatus.UPLOADED)
            .map(MessageContentAttachment::getUuid)
            .toList();
        List<UUID> newAttachmentUuids = CollectionUtils.emptyIfNull(messageContentAttachments).stream()
            .filter(messageContentAttachment -> messageContentAttachment.getStatus() == MessageContentAttachmentStatus.UPLOADED)
            .map(MessageContentAttachment::getUuid)
            .toList();

        // remove attachments that are no longer associated
        currentAttachmentUuids.stream()
            .filter(attachmentUuid -> !newAttachmentUuids.contains(attachmentUuid))
            .forEach(
                attachmentUuid -> {
                    try {
                        delete(messageContent.getMessage(), messageContent.getUuid(), attachmentUuid, securedInfo);
                    } catch (MessageContentNotFoundException | MessageContentNotMatchingException | MessageContentAttachmentException | CanvasApiException | IOException | MessageContentAttachmentNotMatchingException e) {
                        log.error("Error deleting existing attachment with UUID: [{}]", attachmentUuid, e);
                    }
                }
            );

        LtiUserEntity ltiUserEntity = ltiUserRepository.findByUserKeyAndPlatformDeployment_KeyId(securedInfo.getUserId(), securedInfo.getPlatformDeploymentId());

        // add attachments that are not yet associated
        return newAttachmentUuids.stream()
            .filter(newAttachmentUuid -> !currentAttachmentUuids.contains(newAttachmentUuid))
            .map(
                newAttachmentUuid -> {
                    try {
                        MessageContentAttachment persistedAttachment = messageContentAttachmentRepository.findByUuid(newAttachmentUuid)
                            .orElseThrow(() -> new MessageContentAttachmentNotMatchingException(String.format("No attachment with UUID: [%s] found.", newAttachmentUuid)));

                        if (persistedAttachment.getStatus() != MessageContentAttachmentStatus.UPLOADED) {
                            // don't copy over non-uploaded attachments
                            return null;
                        }

                        MessageContentAttachment newMessageContentAttachment = messageContentAttachmentRepository.saveAndFlush(
                            MessageContentAttachment.builder()
                                .canvasId(persistedAttachment.getCanvasId())
                                .content(messageContent)
                                .displayName(persistedAttachment.getDisplayName())
                                .fileName(persistedAttachment.getFileName())
                                .contentType(persistedAttachment.getContentType())
                                .size(persistedAttachment.getSize())
                                .status(MessageContentAttachmentStatus.UPLOADED)
                                .build()
                        );

                        Thread thread = new Thread(
                            () ->
                                {
                                    try {
                                        UploadOptions uploadOptions = new UploadOptions(persistedAttachment.getFileName());
                                        uploadOptions.parentFolderPath("conversation attachments");
                                        uploadOptions.size(persistedAttachment.getSize());
                                        uploadOptions.onDuplicateRename();
                                        Deposit deposit = canvasAPIClient.initializeFileUpload(ltiUserEntity, uploadOptions)
                                            .orElseThrow(() -> new MessageContentAttachmentException(String.format("Error initializing file upload [%s] in Canvas for user ID: [%s]", persistedAttachment.getFileName(), ltiUserEntity.getUserId()), null));

                                        File file;

                                        try (InputStream inputStream = URI.create(persistedAttachment.getUrl()).toURL().openStream()) {
                                            file = canvasAPIClient.uploadFile(ltiUserEntity, deposit, inputStream, persistedAttachment.getFileName())
                                                .orElseThrow(() -> new MessageContentAttachmentException(String.format("Error processing file upload [%s] in Canvas for user ID: [%s]", persistedAttachment.getFileName(), ltiUserEntity.getUserId())));

                                            newMessageContentAttachment.setDisplayName(file.getDisplayName());
                                            newMessageContentAttachment.setFileName(file.getFilename());
                                            newMessageContentAttachment.setUrl(file.getUrl());

                                            messageContentAttachmentRepository.save(newMessageContentAttachment);
                                        } catch (Exception e) {
                                            log.error("Error uploading file ID: [{}] to Canvas for message content with ID: [{}]", persistedAttachment.getId(), messageContent.getId(), e);
                                        }
                                    } catch (MessageContentAttachmentException | CanvasApiException e) {
                                        log.error(e.getMessage(), e);
                                    }
                                }
                        );

                        thread.start();

                        return newMessageContentAttachment;
                    } catch (MessageContentAttachmentNotMatchingException e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }
            )
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public List<MessageContentAttachmentDto> toDto(List<MessageContentAttachment> messageContentAttachments) {
        return CollectionUtils.emptyIfNull(messageContentAttachments).stream()
            .filter(messageContentAttachment -> messageContentAttachment.getStatus() == MessageContentAttachmentStatus.UPLOADED)
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
            .contentType(messageContentAttachment.getContentType())
            .size(messageContentAttachment.getSize())
            .status(messageContentAttachment.getStatus())
            .url(messageContentAttachment.getUrl())
            .build();
    }

}
