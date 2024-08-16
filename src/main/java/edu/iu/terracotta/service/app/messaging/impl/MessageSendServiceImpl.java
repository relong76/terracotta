package edu.iu.terracotta.service.app.messaging.impl;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.Participant;
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

        List<Participant> participants = participantRepository.findByExperiment_ExperimentId(message.getExperimentId());

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

                    Optional<Participant> participant = participants.stream()
                        .filter(p -> p.getLtiUserEntity().getUserId() == ltiUserEntity.getUserId())
                        .findFirst();

                    if (participant.isEmpty()) {
                        // no participant found for user; don't add to recipient list
                        return null;
                    }

                    if (message.getGroup().getConfiguration().isToConsentedOnly() && BooleanUtils.isNotTrue(participant.get().getConsent())) {
                        // is send to consented only and user has not consented; don't add to recipients list
                        return null;
                    }

                    if (participant.get().getGroup() == null && BooleanUtils.isTrue(message.getCondition().getDefaultCondition())) {
                        // no group assigned to participant but is a default condition message; add to recipient list
                        return ltiUserEntity;
                    }

                    if (participant.get().getGroup() == null && BooleanUtils.isNotTrue(message.getCondition().getDefaultCondition())) {
                        // no group assigned to participant and is not a default condition message; don't add to recipient list
                        return null;
                    }

                    if (!participant.get().getGroup().getGroupId().equals(message.getGroupId())) {
                        // participant is not in the group; don't add to recipient list
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
