package edu.iu.terracotta.controller.app.messaging;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.iu.terracotta.exceptions.BadTokenException;
import edu.iu.terracotta.exceptions.ExperimentNotMatchingException;
import edu.iu.terracotta.exceptions.ExposureNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotFoundException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupNotMatchingException;
import edu.iu.terracotta.exceptions.messaging.MessageGroupOwnerNotMatchingException;
import edu.iu.terracotta.model.app.Exposure;
import edu.iu.terracotta.model.app.messaging.MessageGroup;
import edu.iu.terracotta.model.app.messaging.dto.MessageGroupDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.service.app.APIJWTService;
import edu.iu.terracotta.service.app.messaging.MessageGroupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@SuppressWarnings({"PMD.GuardLogStatement"})
@RequestMapping(value = MessageGroupController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageGroupController {

    public static final String REQUEST_ROOT = "api/experiments/{experimentId}/exposures/{exposureId}/messaging/group";

    @Autowired private APIJWTService apijwtService;
    @Autowired private MessageGroupService messageGroupService;

    @GetMapping("/{uuid}")
    public ResponseEntity<MessageGroupDto> get(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID uuid, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        MessageGroup messageGroup;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupService.get(messageGroup), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<MessageGroupDto>> getAll(@PathVariable long experimentId, @PathVariable long exposureId, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);

        try {
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | ExposureNotMatchingException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(messageGroupService.getAll(experimentId, exposureId, securedInfo), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MessageGroupDto> post(@PathVariable long experimentId, @PathVariable long exposureId, @RequestParam(name = "single", defaultValue = "false") boolean single, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Exposure exposure = null;

        try {
            apijwtService.experimentAllowed(securedInfo, experimentId);
            exposure = apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | ExposureNotMatchingException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(messageGroupService.create(exposure, single, securedInfo), HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<MessageGroupDto> put(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID uuid, @RequestBody MessageGroupDto messageGroupDto, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        MessageGroup messageGroup;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupService.update(messageGroupDto, messageGroup), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{uuid}/send")
    public ResponseEntity<MessageGroupDto> send(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID uuid, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        MessageGroup messageGroup;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupService.send(messageGroup), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<MessageGroupDto> delete(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID uuid, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        MessageGroup messageGroup;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | ExposureNotMatchingException | MessageGroupNotFoundException e) {
            log.error("Exception occurred deleting message with UUID: [{}] for user lmsUserId: [{}]", uuid, securedInfo.getCanvasUserId());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }


        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupService.delete(messageGroup), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PostMapping("/{uuid}/move")
    public ResponseEntity<MessageGroupDto> move(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID uuid, @RequestBody MessageGroupDto messageGroupDto, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Exposure newExposure;
        MessageGroup messageGroup;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
            newExposure = apijwtService.exposureAllowed(securedInfo, experimentId, messageGroupDto.getExposureId());
        } catch (BadTokenException | ExperimentNotMatchingException | ExposureNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | MessageGroupNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupService.move(newExposure, messageGroup), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{uuid}/duplicate")
    public ResponseEntity<MessageGroupDto> duplicate(@PathVariable long experimentId, @PathVariable long exposureId, @PathVariable UUID uuid, HttpServletRequest req) {
        SecuredInfo securedInfo = apijwtService.extractValues(req, false);
        Exposure exposure;
        MessageGroup messageGroup;

        try {
            messageGroup = apijwtService.messagingGroupAllowed(securedInfo, exposureId, uuid);
            apijwtService.experimentAllowed(securedInfo, experimentId);
            exposure = apijwtService.exposureAllowed(securedInfo, experimentId, exposureId);
        } catch (BadTokenException | ExperimentNotMatchingException | ExposureNotMatchingException | MessageGroupOwnerNotMatchingException | MessageGroupNotMatchingException | MessageGroupNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!apijwtService.isInstructorOrHigher(securedInfo)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(messageGroupService.duplicate(messageGroup, exposure, securedInfo), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
