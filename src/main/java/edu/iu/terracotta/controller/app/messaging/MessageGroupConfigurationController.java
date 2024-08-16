package edu.iu.terracotta.controller.app.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.iu.terracotta.exceptions.BadTokenException;
import edu.iu.terracotta.exceptions.ExperimentNotMatchingException;
import edu.iu.terracotta.exceptions.ExposureNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupConfigurationNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupOwnerNotMatchingException;
import edu.iu.terracotta.model.app.messaging.MessageGroupConfiguration;
import edu.iu.terracotta.model.app.messaging.dto.MessageGroupConfigurationDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.service.app.APIJWTService;
import edu.iu.terracotta.service.app.messaging.MessageGroupConfigurationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@SuppressWarnings({"PMD.GuardLogStatement"})
@RequestMapping(value = MessageGroupConfigurationController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageGroupConfigurationController {

    public static final String REQUEST_ROOT = "api/experiments/{experimentId}/exposures/{exposureId}/messaging/group/{groupUuid}/configuration";

    @Autowired private APIJWTService apijwtService;
    @Autowired private MessageGroupConfigurationService messageGroupConfigurationService;

    @PutMapping("/{uuid}")
    public ResponseEntity<MessageGroupConfigurationDto> put(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID groupUuid, @PathVariable UUID uuid, @RequestBody MessageGroupConfigurationDto messageGroupConfigurationDto, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        MessageGroupConfiguration messageGroupConfiguration;

        try {
            apijwtService.messagingGroupAllowed(securedInfo, exposureId, groupUuid);
            messageGroupConfiguration = apijwtService.messagingGroupConfigurationAllowed(securedInfo, groupUuid, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageGroupConfigurationNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupConfigurationService.update(messageGroupConfigurationDto, messageGroupConfiguration), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping()
    public ResponseEntity<List<MessageGroupConfigurationDto>> putAll(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable String groupUuid, @RequestBody List<MessageGroupConfigurationDto> messageGroupConfigurationDtos, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        List<MessageGroupConfiguration> messageGroupConfigurations = new ArrayList<>();

        try {
            for (MessageGroupConfigurationDto messageGroupConfigurationDto : messageGroupConfigurationDtos) {
                apijwtService.messagingGroupAllowed(securedInfo, exposureId, messageGroupConfigurationDto.getGroupId());
                messageGroupConfigurations.add(apijwtService.messagingGroupConfigurationAllowed(securedInfo, messageGroupConfigurationDto.getGroupId(), messageGroupConfigurationDto.getId()));
            }

            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException | MessageGroupConfigurationNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupConfigurationService.updateAll(messageGroupConfigurationDtos, messageGroupConfigurations), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
