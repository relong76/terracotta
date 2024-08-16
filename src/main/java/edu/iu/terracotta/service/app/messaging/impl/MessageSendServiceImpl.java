package edu.iu.terracotta.service.app.messaging.impl;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.repository.LtiUserRepository;
import edu.iu.terracotta.repository.ParticipantRepository;
import edu.iu.terracotta.service.app.messaging.MessageSendService;
import edu.iu.terracotta.service.canvas.CanvasAPIClient;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.requestOptions.GetUsersInCourseOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class MessageSendServiceImpl implements MessageSendService {

    @Autowired private LtiUserRepository ltiUserRepository;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private CanvasAPIClient canvasAPIClient;

    @Override
    public List<LtiUserEntity> getRecipients(Message message) throws CanvasApiException {
        GetUsersInCourseOptions getUsersInCourseOptions = new GetUsersInCourseOptions(getCanvasCourseId(message));
        getUsersInCourseOptions.enrollmentState(
            Arrays.asList(
                GetUsersInCourseOptions.EnrollmentState.ACTIVE,
                GetUsersInCourseOptions.EnrollmentState.INVITED
            )
        );
        getUsersInCourseOptions.enrollmentType(
            Collections.singletonList(
                GetUsersInCourseOptions.EnrollmentType.STUDENT
            )
        );
        List<User> students = canvasAPIClient
            .listUsersForCourse(
                getUsersInCourseOptions,
                message.getOwner()
            );

        List<Long> consentedParticipantUserIds = participantRepository.findByExperiment_ExperimentId(message.getExperimentId()).stream()
            .filter(participant -> BooleanUtils.isTrue(participant.getConsent()))
            .map(participant -> participant.getLtiUserEntity().getUserId())
            .toList();

        return students.stream()
            .map(
                student -> {
                    // user's LMS user ID is not always available in the database; try with email and deployment key ID
                    LtiUserEntity ltiUserEntity = ltiUserRepository.findByEmailAndPlatformDeployment_KeyId(student.getEmail(), message.getPlatformDeployment().getKeyId());

                    if (ltiUserEntity == null || ltiUserEntity.getLmsUserId() != null && student.getId() != Long.parseLong(ltiUserEntity.getLmsUserId())) {
                        // wrong LMS user found; don't add message log
                        log.info(
                            "No Terracotta user found with email: [{}] and platform deployment key ID: [{}]. Cannot add to recipients list.",
                            student.getEmail(),
                            message.getPlatformDeployment().getKeyId());
                        return null;
                    }

                    if (ltiUserEntity.getLmsUserId() == null) {
                        // set LMS user ID for the user
                        log.info("Setting LMS user ID: [{}] to Terracotta user ID: [{}]", student.getId(), ltiUserEntity.getUserId());
                        ltiUserEntity.setLmsUserId(Long.toString(student.getId()));
                        ltiUserRepository.save(ltiUserEntity);
                    }

                    // TODO fix this
                    if (!consentedParticipantUserIds.contains(ltiUserEntity.getUserId())) {
                        // user not consented; don't add as recipient
                        return null;
                    }

                    return ltiUserEntity;
                }
            )
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public String getCanvasCourseId(Message message) {
        return StringUtils.substringBetween(
            message.getExperiment().getLtiContextEntity().getContext_memberships_url(),
            "courses/",
            "/names"
        );
    }

}
