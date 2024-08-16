package edu.iu.terracotta.service.canvas;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.canvas.AssignmentExtended;
import edu.iu.terracotta.model.canvas.ConversationOptions;
import edu.iu.terracotta.model.canvas.CourseExtended;
import edu.ksu.canvas.model.Conversation;
import edu.ksu.canvas.model.Deposit;
import edu.ksu.canvas.model.File;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.model.assignment.Submission;
import edu.ksu.canvas.requestOptions.CreateConversationOptions;
import edu.ksu.canvas.requestOptions.GetSingleConversationOptions;
import edu.ksu.canvas.requestOptions.GetUsersInCourseOptions;
import edu.ksu.canvas.requestOptions.UploadOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface CanvasAPIClient {

    Optional<AssignmentExtended> createCanvasAssignment(LtiUserEntity apiUser, AssignmentExtended canvasAssignment, String canvasCourseId) throws CanvasApiException;
    List<AssignmentExtended> listAssignments(LtiUserEntity apiUser, String canvasCourseId) throws CanvasApiException;
    List<AssignmentExtended> listAssignments(String baseUrl, String canvasCourseId, String tokenOverride) throws CanvasApiException;
    List<Submission> listSubmissions(LtiUserEntity apiUser, Long assignmentId, String canvasCourseId) throws CanvasApiException, IOException;
    Optional<AssignmentExtended> checkAssignmentExists(LtiUserEntity apiUser, Long assignmentId, String canvasCourseId) throws CanvasApiException;
    Optional<AssignmentExtended> listAssignment(LtiUserEntity apiUser, String canvasCourseId, long assignmentId) throws CanvasApiException;
    Optional<AssignmentExtended> editAssignment(LtiUserEntity apiUser, AssignmentExtended assignmentExtended, String canvasCourseId) throws CanvasApiException;
    Optional<AssignmentExtended> editAssignment(String baseUrl, AssignmentExtended assignmentExtended, String canvasCourseId, String tokenOverride) throws CanvasApiException;
    Optional<AssignmentExtended> deleteAssignment(LtiUserEntity apiUser, AssignmentExtended assignmentExtended, String canvasCourseId) throws CanvasApiException;
    List<CourseExtended> listCoursesForUser(String baseUrl, String canvasUserId, String tokenOverride) throws CanvasApiException;
    Optional<CourseExtended> editCourse(String baseUrl, CourseExtended courseExtended, String canvasCourseId, String tokenOverride) throws CanvasApiException;
    List<Conversation> sendConversation(CreateConversationOptions createConversationOptions, LtiUserEntity apiUser) throws CanvasApiException;
    Optional<Conversation> getConversation(GetSingleConversationOptions getSingleConversationOptions, LtiUserEntity apiUser) throws CanvasApiException;
    List<Conversation> getConversations(ConversationOptions conversationOptions, LtiUserEntity apiUser) throws CanvasApiException;
    List<User> listUsersForCourse(GetUsersInCourseOptions getUsersInCourseOptions, LtiUserEntity apiUser) throws CanvasApiException;
    Optional<Deposit> initializeFileUpload(LtiUserEntity apiUser, UploadOptions uploadOptions) throws CanvasApiException;
    Optional<File> uploadFile(LtiUserEntity apiUser, Deposit deposit, InputStream inputStream, String filename) throws CanvasApiException;
    Optional<File> getFile(LtiUserEntity apiUser, long canvasFileId) throws CanvasApiException;
    Optional<File> deleteFile(LtiUserEntity apiUser, long canvasFileId) throws CanvasApiException;

}
