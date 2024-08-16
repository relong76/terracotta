package edu.iu.terracotta.service.canvas.impl;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.LMSOAuthException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.canvas.AssignmentExtended;
import edu.iu.terracotta.model.canvas.CanvasAPITokenEntity;
import edu.iu.terracotta.model.canvas.ConversationOptions;
import edu.iu.terracotta.model.canvas.CourseExtended;
import edu.iu.terracotta.service.canvas.AssignmentReaderExtended;
import edu.iu.terracotta.service.canvas.AssignmentWriterExtended;
import edu.iu.terracotta.service.canvas.CanvasAPIClient;
import edu.iu.terracotta.service.canvas.ConversationReaderExtended;
import edu.iu.terracotta.service.canvas.CourseReaderExtended;
import edu.iu.terracotta.service.canvas.CourseWriterExtended;
import edu.iu.terracotta.service.canvas.FileWriterExtended;
import edu.ksu.canvas.exception.CanvasException;
import edu.ksu.canvas.exception.ObjectNotFoundException;
import edu.ksu.canvas.interfaces.CanvasReader;
import edu.ksu.canvas.interfaces.CanvasWriter;
import edu.ksu.canvas.interfaces.ConversationReader;
import edu.ksu.canvas.interfaces.ConversationWriter;
import edu.ksu.canvas.interfaces.FileReader;
import edu.ksu.canvas.interfaces.SubmissionReader;
import edu.ksu.canvas.interfaces.UserReader;
import edu.ksu.canvas.model.Conversation;
import edu.ksu.canvas.model.Deposit;
import edu.ksu.canvas.model.File;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.model.assignment.Submission;
import edu.ksu.canvas.oauth.NonRefreshableOauthToken;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.CreateConversationOptions;
import edu.ksu.canvas.requestOptions.GetSingleAssignmentOptions;
import edu.ksu.canvas.requestOptions.GetSingleConversationOptions;
import edu.ksu.canvas.requestOptions.GetSubmissionsOptions;
import edu.ksu.canvas.requestOptions.GetUsersInCourseOptions;
import edu.ksu.canvas.requestOptions.ListCourseAssignmentsOptions;
import edu.ksu.canvas.requestOptions.ListUserCoursesOptions;
import edu.ksu.canvas.requestOptions.UploadOptions;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class CanvasAPIClientImpl implements CanvasAPIClient {

    @Autowired private CanvasOAuthServiceImpl canvasOAuthService;

    @Value("${app.token.logging.enabled:true}")
    private boolean tokenLoggingEnabled;

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENT_CREATE}
     * </ul>
     */
    @Override
    public Optional<AssignmentExtended> createCanvasAssignment(LtiUserEntity apiUser, AssignmentExtended canvasAssignment, String canvasCourseId) throws CanvasApiException {
        try {
            return getWriter(apiUser, AssignmentWriterExtended.class).createAssignment(canvasCourseId, canvasAssignment);
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to create Assignment in Canvas course by ID [" + canvasCourseId + "]", ex);
        }
    }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENTS_LIST}
     * </ul>
     */
    @Override
    public List<AssignmentExtended> listAssignments(LtiUserEntity apiUser, String canvasCourseId) throws CanvasApiException {
        try {
            return getReader(apiUser, AssignmentReaderExtended.class).listCourseAssignments(new ListCourseAssignmentsOptions(canvasCourseId));
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to get the list of assignments Canvas course [" + canvasCourseId + "]", ex);
        }
    }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENTS_LIST}
     * </ul>
     */
    @Override
    public List<AssignmentExtended> listAssignments(String baseUrl, String canvasCourseId, String tokenOverride) throws CanvasApiException {
        try {
            return getReader(baseUrl, AssignmentReaderExtended.class, tokenOverride).listCourseAssignments(new ListCourseAssignmentsOptions(canvasCourseId));
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to get the list of assignments Canvas course [" + canvasCourseId + "]", ex);
        }
    }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENT_GET}
     * </ul>
     */
    @Override
    public Optional<AssignmentExtended> listAssignment(LtiUserEntity apiUser, String canvasCourseId, long assignmentId) throws CanvasApiException {
        try {
            return getReader(apiUser, AssignmentReaderExtended.class).getSingleAssignment(new GetSingleAssignmentOptions(canvasCourseId, assignmentId));
        } catch (ObjectNotFoundException ex) {
            return Optional.empty();
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to get the assignments with id [" + assignmentId + "] from canvas course [" + canvasCourseId + "]", ex);
        }
    }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENT_EDIT}
     * </ul>
     */
    @Override
    public Optional<AssignmentExtended> editAssignment(LtiUserEntity apiUser, AssignmentExtended assignmentExtended, String canvasCourseId) throws CanvasApiException {
        try {
            return getWriter(apiUser, AssignmentWriterExtended.class).editAssignment(canvasCourseId, assignmentExtended);
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to edit the assignments with id [" + assignmentExtended.getId() + "] from canvas course [" + canvasCourseId + "]", ex);
        }
    }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENT_EDIT}
     * </ul>
     */
    @Override
    public Optional<AssignmentExtended> editAssignment(String baseUrl, AssignmentExtended assignmentExtended, String canvasCourseId, String tokenOverride) throws CanvasApiException {
        try {
            return getWriter(baseUrl, AssignmentWriterExtended.class, tokenOverride).editAssignment(canvasCourseId, assignmentExtended);
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to edit the assignments with id [" + assignmentExtended.getId() + "] from canvas course [" + canvasCourseId + "]", ex);
        }
    }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENT_DELETE}
     * </ul>
     */
    @Override
    public Optional<AssignmentExtended> deleteAssignment(LtiUserEntity apiUser, AssignmentExtended assignmentExtended, String canvasCourseId) throws CanvasApiException {
        try {
            return getWriter(apiUser, AssignmentWriterExtended.class).deleteAssignment(canvasCourseId, assignmentExtended.getId());
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to edit the assignments with id [" + assignmentExtended.getId() + "] from canvas course [" + canvasCourseId + "]", ex);
        }
    }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_SUBMISSIONS_FOR_ASSIGNMENT_LIST}
     * </ul>
     */
    @Override
    public List<Submission> listSubmissions(LtiUserEntity apiUser, Long assignmentId, String canvasCourseId) throws CanvasApiException, IOException {
        GetSubmissionsOptions submissionsOptions = new GetSubmissionsOptions(canvasCourseId, assignmentId);
        submissionsOptions.includes(Collections.singletonList(GetSubmissionsOptions.Include.USER));

        return getReader(apiUser, SubmissionReader.class).getCourseSubmissions(submissionsOptions);
    }

    // @Override
    // public List<Submission> listSubmissionsForGivenUser(LtiUserEntity apiUser, Integer assignmentId,
    //         String canvasCourseId, String canvasUserId) throws CanvasApiException, IOException {
    //     PlatformDeployment platformDeployment = apiUser.getPlatformDeployment();
    //     String canvasBaseUrl = platformDeployment.getBaseUrl();
    //     String accessToken = getAccessToken(apiUser);
    //     OauthToken oauthToken = new NonRefreshableOauthToken(accessToken);
    //     CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
    //     SubmissionImpl submissionReader = apiFactory.getReader(SubmissionImpl.class, oauthToken);
    //     GetSubmissionsOptions submissionsOptions = new GetSubmissionsOptions(canvasCourseId, assignmentId);
    //     submissionsOptions.includes(Collections.singletonList(GetSubmissionsOptions.Include.USER));
    //     return submissionReader.getCourseSubmissions(submissionsOptions);
    // }

    // @Override
    // public Optional<Progress> postSubmission(LtiUserEntity apiUser, edu.iu.terracotta.model.app.Submission submission,
    //         Float maxTerracottaScore)
    //         throws CanvasApiException, IOException {

    //     String canvasCourseId = getCanvasCourseId(submission.getParticipant().getLtiMembershipEntity().getContext().getContext_memberships_url());
    //     int assignmentId = Integer.parseInt(submission.getAssessment().getTreatment().getAssignment().getLmsAssignmentId());
    //     String canvasUserId = submission.getParticipant().getLtiUserEntity().getLmsUserId();
    //     Optional<AssignmentExtended> assignmentExtended = listAssignment(apiUser, canvasCourseId, assignmentId);
    //     if (assignmentExtended.isEmpty()) {
    //         throw new CanvasApiException(
    //                 "Failed to get the assignments with id [" + assignmentId + "] from canvas course [" + canvasCourseId + "]");
    //     }
    //     Double maxCanvasScore = assignmentExtended.get().getPointsPossible();

    //     Double grade = Double.valueOf("0");
    //     if (submission.getTotalAlteredGrade() != null) {
    //         grade = Double.parseDouble(submission.getTotalAlteredGrade().toString());
    //     } else if (submission.getAlteredCalculatedGrade() != null) {
    //         grade = Double.parseDouble(submission.getAlteredCalculatedGrade().toString());
    //     } else {
    //         grade = Double.parseDouble(submission.getCalculatedGrade().toString());
    //     }
    //     grade = grade * maxCanvasScore / Double.parseDouble(maxTerracottaScore.toString());

    //     return postGrade(apiUser, canvasCourseId, assignmentId, canvasUserId, grade);
    // }

    // @Override
    // public Optional<Progress> postConsentSubmission(LtiUserEntity apiUser, Participant participant)
    //         throws CanvasApiException, IOException {
    //     String canvasCourseId = getCanvasCourseId(participant.getLtiMembershipEntity().getContext().getContext_memberships_url());
    //     int assignmentId = Integer.parseInt(participant.getExperiment().getConsentDocument().getLmsAssignmentId());
    //     String canvasUserId = participant.getLtiUserEntity().getLmsUserId();
    //     Optional<AssignmentExtended> assignmentExtended = listAssignment(apiUser, canvasCourseId, assignmentId);
    //     if (assignmentExtended.isEmpty()) {
    //         throw new CanvasApiException(
    //                 "Failed to get the assignments with id [" + assignmentId + "] from canvas course [" + canvasCourseId + "]");
    //     }

    //     Double grade = Double.valueOf("1.0");

    //     return postGrade(apiUser, canvasCourseId, assignmentId, canvasUserId, grade);
    // }

    // private Optional<Progress> postGrade(LtiUserEntity apiUser, String canvasCourseId, int assignmentId,
    //         String canvasUserId, Double grade) throws IOException, CanvasApiException {
    //     PlatformDeployment platformDeployment = apiUser.getPlatformDeployment();
    //     String canvasBaseUrl = platformDeployment.getBaseUrl();
    //     String accessToken = getAccessToken(apiUser);
    //     OauthToken oauthToken = new NonRefreshableOauthToken(accessToken);
    //     CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
    //     SubmissionWriter submissionWriter = apiFactory.getWriter(SubmissionWriter.class, oauthToken);
    //     MultipleSubmissionsOptions multipleSubmissionsOptions = new MultipleSubmissionsOptions(canvasCourseId, assignmentId, new HashMap<>());
    //     MultipleSubmissionsOptions.StudentSubmissionOption submissionsOptions =
    //             multipleSubmissionsOptions.createStudentSubmissionOption(
    //                     null,
    //                     grade.toString(),
    //                     false,
    //                     false,
    //                     null,
    //                     null
    //             );
    //     HashMap<String, MultipleSubmissionsOptions.StudentSubmissionOption> submissionOptionHashMap = new HashMap<>();
    //     submissionOptionHashMap.put(canvasUserId, submissionsOptions);
    //     multipleSubmissionsOptions.setStudentSubmissionOptionMap(submissionOptionHashMap);
    //     submissionWriter.gradeMultipleSubmissionsByCourse(multipleSubmissionsOptions);
    //     return submissionWriter.gradeMultipleSubmissionsByCourse(multipleSubmissionsOptions);
    // }

    /**
     * required scopes:
     * <ul>
     * <li>{@value #SCOPE_ASSIGNMENT_GET}
     * </ul>
     */
    @Override
    public Optional<AssignmentExtended> checkAssignmentExists(LtiUserEntity apiUser, Long assignmentId, String canvasCourseId) throws CanvasApiException {
        try {
            return getReader(apiUser, AssignmentReaderExtended.class).getSingleAssignment(new GetSingleAssignmentOptions(canvasCourseId, assignmentId));
        } catch (ObjectNotFoundException e) {
            return Optional.empty();
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException("Failed to get the Assignment in Canvas course by ID [" + canvasCourseId + "]", ex);
        }
    }

    @Override
    public List<CourseExtended> listCoursesForUser(String baseUrl, String canvasUserId, String tokenOverride) throws CanvasApiException {
        try {
            return getReader(baseUrl, CourseReaderExtended.class, tokenOverride).listCoursesForUser(new ListUserCoursesOptions(canvasUserId));
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException(String.format("Failed to get the courses from canvas for user ID [%s]", canvasUserId), ex);
        }
    }

    @Override
    public Optional<CourseExtended> editCourse(String baseUrl, CourseExtended courseExtended, String canvasCourseId, String tokenOverride) throws CanvasApiException {
        try {
            return getWriter(baseUrl, CourseWriterExtended.class, tokenOverride).editCourse(canvasCourseId, courseExtended);
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException(String.format("Failed to edit the course with ID [%s] in Canvas", canvasCourseId), ex);
        }
    }

    @Override
    public List<Conversation> sendConversation(CreateConversationOptions createConversationOptions, LtiUserEntity apiUser) throws CanvasApiException {
        try {
            return getWriter(apiUser, ConversationWriter.class).createConversation(createConversationOptions);
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException(
                String.format("Failed to send a conversation from Canvas user ID: [%s] to Canvas user ID: [%s] in Canvas",
                    apiUser.getLmsUserId(),
                    StringUtils.join(createConversationOptions.getOptionsMap().get("recipients[]"), ',')
                ),
                ex
            );
        }
    }

    @Override
    public Optional<Conversation> getConversation(GetSingleConversationOptions getSingleConversationOptions, LtiUserEntity apiUser) throws CanvasApiException {
        try {
            return getReader(apiUser, ConversationReader.class).getSingleConversation(getSingleConversationOptions);
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException(String.format("Failed to get conversation with ID: [%s] for Canvas user ID: [%s]", getSingleConversationOptions.getConversationId(), apiUser.getLmsUserId()), ex);
        }
    }

    @Override
    public List<Conversation> getConversations(ConversationOptions conversationOptions, LtiUserEntity apiUser) throws CanvasApiException {
        try {
            return getReader(apiUser, ConversationReaderExtended.class).getConversations();
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException(String.format("Failed to get the list of conversations for Canvas user ID: [%s]", apiUser.getLmsUserId()), ex);
        }
    }

    @Override
    public List<User> listUsersForCourse(GetUsersInCourseOptions getUsersInCourseOptions, LtiUserEntity apiUser) throws CanvasApiException {
        try {
            return getReader(apiUser, UserReader.class).getUsersInCourse(getUsersInCourseOptions);
        } catch (IOException | CanvasException ex) {
            throw new CanvasApiException(String.format("Failed to get the list of users for Canvas course ID: [%s] for Canvas user ID: [%s]", getUsersInCourseOptions.getCourseId(), apiUser.getLmsUserId()), ex);
        }
    }

    @Override
    public Optional<Deposit> initializeFileUpload(LtiUserEntity apiUser, UploadOptions uploadOptions) throws CanvasApiException {
        try {
            return getWriter(apiUser, FileWriterExtended.class).initializeUpload(uploadOptions);
        } catch (IOException | CanvasException e) {
            throw new CanvasApiException(String.format("Failed to initialize file upload in Canvas for user ID [%s]", apiUser.getLmsUserId()), e);
        }
    }

    @Override
    public Optional<File> uploadFile(LtiUserEntity apiUser, Deposit deposit, InputStream inputStream, String filename) throws CanvasApiException {
        try {
            return getWriter(apiUser, FileWriterExtended.class).upload(deposit, inputStream, filename);
        } catch (IOException | CanvasException e) {
            throw new CanvasApiException(String.format("Failed to upload file to URL: [%s] in Canvas for user ID [%s]", deposit.getUploadUrl(), apiUser.getLmsUserId()), e);
        }
    }

    @Override
    public Optional<File> getFile(LtiUserEntity apiUser, long canvasFileId) throws CanvasApiException {
        try {
            return getReader(apiUser, FileReader.class).getFile(Long.toString(canvasFileId));
        } catch (IOException | CanvasException e) {
            throw new CanvasApiException(String.format("Failed to get for with Canvas file ID: [%s] for Canvas user ID: [%s]", canvasFileId, apiUser.getLmsUserId()), e);
        }
    }

    @Override
    public Optional<File> deleteFile(LtiUserEntity apiUser, long canvasFileId) throws CanvasApiException {
        try {
            return getWriter(apiUser, FileWriterExtended.class).delete(canvasFileId);
        } catch (IOException | CanvasException e) {
            throw new CanvasApiException(String.format("Failed to delete file ID: [{}] in Canvas for user ID [%s]", canvasFileId, apiUser.getLmsUserId()), e);
        }
    }

    private <T extends CanvasWriter<?, T>> T getWriter(LtiUserEntity apiUser, Class<T> clazz) throws CanvasApiException {
        return getWriterInternal(apiUser, clazz, getOauthToken(apiUser));
    }

    private <T extends CanvasWriter<?, T>> T getWriter(String baseUrl, Class<T> clazz, String tokenOverride) throws CanvasApiException {
        return getWriterInternal(baseUrl, clazz, getOauthToken(null, tokenOverride));
    }

    private <T extends CanvasReader<?, T>> T getReader(LtiUserEntity apiUser, Class<T> clazz) throws CanvasApiException {
        return getReaderInternal(apiUser, clazz, getOauthToken(apiUser));
    }

    private <T extends CanvasReader<?, T>> T getReader(String baseUrl, Class<T> clazz, String tokenOverride) throws CanvasApiException {
        return getReaderInternal(baseUrl, clazz, getOauthToken(null, tokenOverride));
    }

    <T extends CanvasReader<?, T>> T getReaderInternal(LtiUserEntity apiUser, Class<T> clazz, OauthToken oauthToken) {
        return getApiFactory(apiUser).getReader(clazz, oauthToken);
    }

    <T extends CanvasReader<?, T>> T getReaderInternal(String baseUrl, Class<T> clazz, OauthToken oauthToken) {
        return getApiFactory(baseUrl).getReader(clazz, oauthToken);
    }

    <T extends CanvasWriter<?, T>> T getWriterInternal(LtiUserEntity apiUser, Class<T> clazz, OauthToken oauthToken) {
        return getApiFactory(apiUser).getWriter(clazz, oauthToken);
    }

    <T extends CanvasWriter<?, T>> T getWriterInternal(String baseUrl, Class<T> clazz, OauthToken oauthToken) {
        return getApiFactory(baseUrl).getWriter(clazz, oauthToken);
    }

    private CanvasApiFactoryExtended getApiFactory(LtiUserEntity apiUser) {
        return new CanvasApiFactoryExtended(apiUser.getPlatformDeployment().getBaseUrl());
    }

    private CanvasApiFactoryExtended getApiFactory(String baseUrl) {
        return new CanvasApiFactoryExtended(baseUrl);
    }

    private OauthToken getOauthToken(LtiUserEntity apiUser) throws CanvasApiException {
        return new NonRefreshableOauthToken(getAccessToken(apiUser, null));
    }

    private OauthToken getOauthToken(LtiUserEntity apiUser, String tokenOverride) throws CanvasApiException {
        return new NonRefreshableOauthToken(getAccessToken(apiUser, tokenOverride));
    }

    private String getAccessToken(LtiUserEntity apiUser, String tokenOverride) throws CanvasApiException {
        if (tokenOverride != null) {
            if (tokenLoggingEnabled) {
                log.debug("Using API token override: '{}'", tokenOverride);
            }

            return tokenOverride;
        }

        String accessToken = null;

        if (canvasOAuthService.isConfigured(apiUser.getPlatformDeployment())) {
            try {
                CanvasAPITokenEntity canvasAPIToken = canvasOAuthService.getAccessToken(apiUser);

                if (tokenLoggingEnabled) {
                    log.debug("Using access token for user {}", apiUser.getUserKey());
                }

                accessToken = canvasAPIToken.getAccessToken();
            } catch (LMSOAuthException e) {
                throw new CanvasApiException(MessageFormat.format("Could not get a Canvas API token for user {0}", apiUser.getUserKey()), e);
            }
        } else if (apiUser.getPlatformDeployment().getApiToken() != null) {
            if (tokenLoggingEnabled) {
                log.debug("Using admin api token configured for platform deployment {}", apiUser.getPlatformDeployment().getKeyId());
            }

            accessToken = apiUser.getPlatformDeployment().getApiToken();
        } else {
            throw new CanvasApiException(MessageFormat.format(
                "Could not get a Canvas API token for platform deployment {0} and user {1}",
                apiUser.getPlatformDeployment().getKeyId(), apiUser.getUserKey())
            );
        }

        return accessToken;
    }

}
