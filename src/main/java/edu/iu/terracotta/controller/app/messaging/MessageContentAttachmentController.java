package edu.iu.terracotta.controller.app.messaging;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.iu.terracotta.exceptions.BadTokenException;
import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.ExperimentNotMatchingException;
import edu.iu.terracotta.exceptions.ExposureNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageContentAttachmentException;
import edu.iu.terracotta.exceptions.messaging.MessageContentAttachmentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupOwnerNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageOwnerNotMatchingException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.dto.MessageContentAttachmentDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.service.app.APIJWTService;
import edu.iu.terracotta.service.app.messaging.MessageContentAttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@SuppressWarnings({"PMD.GuardLogStatement"})
@RequestMapping(value = MessageContentAttachmentController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageContentAttachmentController {

    public static final String REQUEST_ROOT = "api/experiments/{experimentId}/exposures/{exposureId}/messaging/group/{groupUuid}/message/{messageUuid}/content/{contentUuid}/file";

    @Autowired private APIJWTService apijwtService;
    @Autowired private MessageContentAttachmentService messageContentAttachmentService;

    @PostMapping
    public ResponseEntity<MessageContentAttachmentDto> post(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID messageUuid, @PathVariable UUID contentUuid, @RequestPart("file") MultipartFile file, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Message message;

        try {
            apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            message = apijwtService.messagingAllowed(securedInfo, groupUuid, messageUuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageOwnerNotMatchingException | MessageNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageContentAttachmentService.upload(message, contentUuid, file, securedInfo), HttpStatus.OK);
        } catch (MessageContentNotFoundException | MessageContentNotMatchingException | MessageContentAttachmentException | CanvasApiException | IOException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<MessageContentAttachmentDto> delete(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID messageUuid, @PathVariable UUID contentUuid, @PathVariable UUID uuid, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Message message;

        try {
            apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            message = apijwtService.messagingAllowed(securedInfo, groupUuid, messageUuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageOwnerNotMatchingException | MessageNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            messageContentAttachmentService.delete(message, contentUuid, uuid, securedInfo);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MessageContentNotFoundException | MessageContentNotMatchingException | MessageContentAttachmentException | CanvasApiException | IOException | MessageContentAttachmentNotMatchingException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

}
