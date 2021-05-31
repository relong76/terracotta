package edu.iu.terracotta.controller.app;

import edu.iu.terracotta.exceptions.AssignmentNotMatchingException;
import edu.iu.terracotta.exceptions.BadTokenException;
import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.ExperimentNotMatchingException;
import edu.iu.terracotta.exceptions.ExposureNotMatchingException;
import edu.iu.terracotta.model.app.dto.AssignmentDto;
import edu.iu.terracotta.model.oauth2.SecurityInfo;
import edu.iu.terracotta.service.app.APIJWTService;
import edu.iu.terracotta.service.app.AssignmentService;
import edu.iu.terracotta.utils.TextConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import edu.iu.terracotta.model.app.Assignment;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;


@Controller
@RequestMapping(value = AssignmentController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class AssignmentController {

    static final Logger log = LoggerFactory.getLogger(AssignmentController.class);
    static final String REQUEST_ROOT = "api/experiments";

    @Autowired
    AssignmentService assignmentService;

    @Autowired
    APIJWTService apijwtService;


    @RequestMapping(value = "/{experiment_id}/exposures/{exposure_id}/assignments", method = RequestMethod.GET, produces = "application/json;")
    @ResponseBody
    public ResponseEntity<List<AssignmentDto>> allAssignmentsByExposure(@PathVariable("experiment_id") long experimentId,
                                                                        @PathVariable("exposure_id") Long exposureId,
                                                                        HttpServletRequest req)
            throws ExperimentNotMatchingException, BadTokenException, ExposureNotMatchingException {

        SecurityInfo securityInfo = apijwtService.extractValues(req, false);
        apijwtService.experimentAllowed(securityInfo, experimentId);
        apijwtService.exposureAllowed(securityInfo, experimentId, exposureId);

        if (apijwtService.isLearnerOrHigher(securityInfo)) {
            List<Assignment> assignmentList = assignmentService.findAllByExposureId(exposureId);

            if (assignmentList.isEmpty()) {
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
            List<AssignmentDto> assignmentDtos = new ArrayList<>();
            for (Assignment assignment : assignmentList) {
                assignmentDtos.add(assignmentService.toDto(assignment));
            }
            return new ResponseEntity<>(assignmentDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/{experiment_id}/exposures/{exposure_id}/assignments/{assignment_id}", method = RequestMethod.GET, produces = "application/json;")
    @ResponseBody
    public ResponseEntity<AssignmentDto> getAssignment(@PathVariable("experiment_id") long experimentId,
                                                       @PathVariable("exposure_id") long exposureId,
                                                       @PathVariable("assignment_id") long assignmentId,
                                                       HttpServletRequest req)
            throws ExperimentNotMatchingException, BadTokenException, AssignmentNotMatchingException {

        SecurityInfo securityInfo = apijwtService.extractValues(req, false);
        apijwtService.experimentAllowed(securityInfo, experimentId);
        apijwtService.assignmentAllowed(securityInfo, experimentId, exposureId, assignmentId);
        //student access? Or should it be instructorOrHigher?
        if (apijwtService.isLearnerOrHigher(securityInfo)) {

            Optional<Assignment> assignmentSearchResult = assignmentService.findById(assignmentId);

            if (!assignmentSearchResult.isPresent()) {
                log.error("assignment in platform {} and context {} and experiment {} and exposure {} with id {} not found",
                        securityInfo.getPlatformDeploymentId(), securityInfo.getContextId(), experimentId, experimentId, assignmentId);

                return new ResponseEntity("assignment in platform " + securityInfo.getPlatformDeploymentId()
                        + " and context " + securityInfo.getContextId() + " and experiment with id " + experimentId + " and exposure id " + exposureId
                        + " with id " + assignmentId + TextConstants.NOT_FOUND_SUFFIX, HttpStatus.NOT_FOUND);
            } else {
                AssignmentDto assignmentDto = assignmentService.toDto(assignmentSearchResult.get());
                return new ResponseEntity<>(assignmentDto, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/{experiment_id}/exposures/{exposure_id}/assignments", method = RequestMethod.POST)
    public ResponseEntity<AssignmentDto> postAssignment(@PathVariable("experiment_id") long experimentId,
                                                        @PathVariable("exposure_id") long exposureId,
                                                        @RequestBody AssignmentDto assignmentDto,
                                                        UriComponentsBuilder ucBuilder,
                                                        HttpServletRequest req)
            throws ExperimentNotMatchingException, ExposureNotMatchingException, BadTokenException {
        log.info("Creating Assignment: {}", assignmentDto);
        SecurityInfo securityInfo = apijwtService.extractValues(req, false);
        apijwtService.experimentAllowed(securityInfo, experimentId);
        apijwtService.exposureAllowed(securityInfo, experimentId, exposureId);

        if (apijwtService.isInstructorOrHigher(securityInfo)) {
            if (assignmentDto.getAssignmentId() != null) {
                log.error("Cannot include id in the POST endpoint. To modify existing exposures you must use PUT");
                return new ResponseEntity("Cannot include id in the POST endpoint. To modify existing exposures you must use PUT", HttpStatus.CONFLICT);
            }

            assignmentDto.setExposureId(exposureId);
            Assignment assignment = null;
            try {
                assignment = assignmentService.fromDto(assignmentDto);
            } catch (DataServiceException e) {
                return new ResponseEntity("Unable to create Assignment: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }

            Assignment assignmentSaved = assignmentService.save(assignment);
            AssignmentDto returnedDto = assignmentService.toDto(assignmentSaved);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ucBuilder.path("/api/experiments/{experiment_id}/exposures/{exposure_id}/assignments/{assignment_id}")
                    .buildAndExpand(assignment.getExposure().getExperiment().getExperimentId(), assignment.getExposure().getExposureId(), assignment.getAssignmentId()).toUri());
            return new ResponseEntity<>(returnedDto, headers, HttpStatus.CREATED);
        } else {
            return new ResponseEntity(TextConstants.NOT_ENOUGH_PERMISSIONS, HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/{experiment_id}/exposures/{exposure_id}/assignments/{assignment_id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> updateAssignment(@PathVariable("experiment_id") Long experimentId,
                                                 @PathVariable("exposure_id") Long exposureId,
                                                 @PathVariable("assignment_id") Long assignmentId,
                                                 @RequestBody AssignmentDto assignmentDto,
                                                 HttpServletRequest req)
            throws ExperimentNotMatchingException, BadTokenException, AssignmentNotMatchingException {

        log.info("Updating assignment with id: {}", assignmentId);
        SecurityInfo securityInfo = apijwtService.extractValues(req, false);
        apijwtService.experimentAllowed(securityInfo, experimentId);
        apijwtService.assignmentAllowed(securityInfo, experimentId, exposureId, assignmentId);

        if(apijwtService.isInstructorOrHigher(securityInfo)) {
            Optional<Assignment> assignmentSearchResult = assignmentService.findById(assignmentId);

            if(!assignmentSearchResult.isPresent()) {
                log.error("Unable to update. Assignment with id {} not found.", assignmentId);
                return new ResponseEntity("Unable to update. Assignment with id " + assignmentId + TextConstants.NOT_FOUND_SUFFIX, HttpStatus.NOT_FOUND);
            }
            Assignment assignmentToChange = assignmentSearchResult.get();
            assignmentToChange.setTitle(assignmentDto.getTitle());
            assignmentToChange.setAssignmentOrder(assignmentDto.getAssignmentOrder());
            assignmentToChange.setLmsAssignmentId(assignmentDto.getLmsAssignmentId());

            assignmentService.saveAndFlush(assignmentToChange);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity(TextConstants.NOT_ENOUGH_PERMISSIONS, HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/{experiment_id}/exposures/{exposure_id}/assignments/{assignment_id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteAssignment(@PathVariable("experiment_id") Long experimentId,
                                                 @PathVariable("exposure_id") Long exposureId,
                                                 @PathVariable("assignment_id") Long assignmentId,
                                                 HttpServletRequest req)
            throws ExperimentNotMatchingException, AssignmentNotMatchingException, BadTokenException {

        SecurityInfo securityInfo = apijwtService.extractValues(req, false);
        apijwtService.experimentAllowed(securityInfo, experimentId);
        apijwtService.assignmentAllowed(securityInfo, experimentId, exposureId, assignmentId);

        if(apijwtService.isInstructorOrHigher(securityInfo)) {
            try{
                assignmentService.deleteById(assignmentId);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (EmptyResultDataAccessException e) {
                log.error(e.getMessage());
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(TextConstants.NOT_ENOUGH_PERMISSIONS, HttpStatus.UNAUTHORIZED);
        }
    }
}