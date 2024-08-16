package edu.iu.terracotta.controller.app.messaging;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.iu.terracotta.exceptions.BadTokenException;
import edu.iu.terracotta.exceptions.ExperimentNotMatchingException;
import edu.iu.terracotta.exceptions.ExposureNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupOwnerNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageOwnerNotMatchingException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.service.app.APIJWTService;
import edu.iu.terracotta.service.app.messaging.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@SuppressWarnings({"PMD.GuardLogStatement"})
@RequestMapping(value = MessageController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageController {

    public static final String REQUEST_ROOT = "api/experiments/{experimentId}/exposures/{exposureId}/messaging/group/{groupUuid}/message";

    @Autowired private APIJWTService apijwtService;
    @Autowired private MessageService messageService;

    @GetMapping("/{uuid}")
    public ResponseEntity<MessageDto> get(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID uuid, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Message message;

        try {
            apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            message = apijwtService.messagingAllowed(securedInfo, groupUuid, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageOwnerNotMatchingException | MessageNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageService.get(message), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<MessageDto> put(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID uuid, @RequestBody MessageDto messageDto, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        MessageGroup messageGroup;
        Message message;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            message = apijwtService.messagingAllowed(securedInfo, groupUuid, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageOwnerNotMatchingException | MessageNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageService.update(messageDto, exposureId, messageGroup, message), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
