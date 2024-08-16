package edu.iu.terracotta.controller.app.messaging;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.iu.terracotta.exceptions.BadTokenException;
import edu.iu.terracotta.exceptions.ExperimentNotMatchingException;
import edu.iu.terracotta.exceptions.ExposureNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageContentNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupOwnerNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageOwnerNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageRuleNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageRuleNotMatchingException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.dto.MessageRuleDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.service.app.APIJWTService;
import edu.iu.terracotta.service.app.messaging.MessageRuleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@SuppressWarnings({"PMD.GuardLogStatement"})
@RequestMapping(value = MessageRuleController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageRuleController {

    public static final String REQUEST_ROOT = "api/experiments/{experimentId}/exposures/{exposureId}/messaging/group/{groupUuid}/message/{messageUuid}/content/{contentUuid}/rule";

    @Autowired private APIJWTService apijwtService;
    @Autowired private MessageRuleService messageRuleService;

    @PostMapping
    public ResponseEntity<MessageRuleDto> post(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID messageUuid, @PathVariable UUID contentUuid, @RequestBody MessageRuleDto messageRuleDto, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Message message;

        try {
            apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            message = apijwtService.messagingAllowed(securedInfo, groupUuid, messageUuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageOwnerNotMatchingException | MessageNotMatchingException | ExposureNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | MessageGroupNotFoundException | MessageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageRuleService.create(messageRuleDto, message, contentUuid), HttpStatus.OK);
        } catch (MessageContentNotFoundException | MessageContentNotMatchingException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<MessageRuleDto> put(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID messageUuid, @PathVariable UUID contentUuid, @PathVariable UUID uuid, @RequestBody MessageRuleDto messageRuleDto, HttpServletRequest req) {
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
            return new ResponseEntity<>(messageRuleService.update(messageRuleDto, message, contentUuid, uuid), HttpStatus.OK);
        } catch (MessageRuleNotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (MessageContentNotMatchingException | MessageContentNotFoundException | MessageRuleNotMatchingException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID messageUuid, @PathVariable UUID contentUuid, @PathVariable UUID uuid, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Message message;

        try {
            apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            message = apijwtService.messagingAllowed(securedInfo, groupUuid, messageUuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (MessageOwnerNotMatchingException | BadTokenException | ExperimentNotMatchingException | MessageNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageNotFoundException e) {
            log.error("Exception occurred deleting message with UUID: [{}] for user lmsUserId: [{}]", uuid, securedInfo.getCanvasUserId());
            return;
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return;
        }

        messageRuleService.delete(message, contentUuid, uuid);
    }

}
