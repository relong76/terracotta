package edu.iu.terracotta.controller.app.messaging;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.iu.terracotta.exceptions.BadTokenException;
import edu.iu.terracotta.exceptions.ExperimentNotMatchingException;
import edu.iu.terracotta.exceptions.ExposureNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageConfigurationNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupOwnerNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageOwnerNotMatchingException;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.MessageConfiguration;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageConfigurationDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.service.app.APIJWTService;
import edu.iu.terracotta.service.app.messaging.MessageConfigurationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@SuppressWarnings({"PMD.GuardLogStatement"})
@RequestMapping(value = MessageConfigurationController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageConfigurationController {

    public static final String REQUEST_ROOT = "api/experiments/{experimentId}/exposures/{exposureId}/messaging/group/{groupUuid}/message/{messageUuid}/configuration";

    @Autowired private APIJWTService apijwtService;
    @Autowired private MessageConfigurationService messageConfigurationService;

    @PutMapping("/{uuid}")
    public ResponseEntity<MessageConfigurationDto> put(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID messageUuid, @PathVariable UUID uuid, @RequestBody MessageConfigurationDto messageConfigurationDto, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        MessageGroup messageGroup;
        Message message;
        MessageConfiguration messageConfiguration;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            message = apijwtService.messagingAllowed(securedInfo, groupUuid, messageUuid);
            messageConfiguration = apijwtService.messagingConfigurationAllowed(securedInfo, messageUuid, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageOwnerNotMatchingException | MessageNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageNotFoundException | MessageConfigurationNotMatchingException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageConfigurationService.update(messageConfigurationDto, messageGroup, message, messageConfiguration), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
