package edu.iu.terracotta.service.app.integrations.impl;

import java.sql.Timestamp;
import java.time.Instant;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenInvalidException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenNotFoundException;
import edu.iu.terracotta.model.app.QuestionSubmission;
import edu.iu.terracotta.model.app.integrations.IntegrationToken;
import edu.iu.terracotta.model.app.integrations.IntegrationTokenLog;
import edu.iu.terracotta.model.app.integrations.enums.IntegrationTokenStatus;
import edu.iu.terracotta.repository.QuestionRepository;
import edu.iu.terracotta.repository.QuestionSubmissionRepository;
import edu.iu.terracotta.repository.SubmissionRepository;
import edu.iu.terracotta.repository.integrations.IntegrationTokenLogRepository;
import edu.iu.terracotta.service.app.integrations.IntegrationScoreService;
import edu.iu.terracotta.service.app.integrations.IntegrationTokenService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings({"PMD.GuardLogStatement"})
public class IntegrationScoreServiceImpl implements IntegrationScoreService {

    @Autowired private IntegrationTokenLogRepository integrationTokenLogRepository;
    @Autowired private QuestionSubmissionRepository questionSubmissionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private IntegrationTokenService integrationTokenService;

    @Override
    public void score(String launchToken, String score) throws IntegrationTokenNotFoundException, DataServiceException, IntegrationTokenInvalidException {
        try {
            // invalidate the token
            IntegrationToken integrationToken = integrationTokenService.redeemToken(launchToken);
            float calculatedScore;

            try {
                calculatedScore = Float.parseFloat(score);
            } catch (Exception e) {
                throw new RuntimeException(
                    String.format(
                        "Error converting calculated score: [%s]. Cannot set score for submission ID: [%s]",
                        score,
                        integrationToken.getSubmission().getSubmissionId()
                    ),
                    e
                );
            }

            if (CollectionUtils.isNotEmpty(integrationToken.getSubmission().getQuestionSubmissions())) {
                // question submissions exist; update the calculated score
                integrationToken.getSubmission().getQuestionSubmissions()
                    .forEach(
                        questionSubmission -> {
                            questionSubmission.setCalculatedPoints(calculatedScore);
                            questionSubmission.setAlteredGrade(0f);
                            questionSubmissionRepository.save(questionSubmission);
                        }
                    );
            } else {
                // create a question submission and set calculated score for the submission
                QuestionSubmission questionSubmission = new QuestionSubmission();
                questionSubmission.setCalculatedPoints(calculatedScore);
                questionSubmission.setAlteredGrade(null);
                questionSubmission.setSubmission(integrationToken.getSubmission());
                questionSubmission.setQuestion(
                    questionRepository.findByAssessment_AssessmentIdAndQuestionId(
                        integrationToken.getSubmission().getAssessment().getAssessmentId(),
                        integrationToken.getSubmission().getAssessment().getQuestions().get(0).getQuestionId()
                    )
                    .orElseThrow(() -> new DataServiceException("Question does not exist or does not belong to the submission and assessment"))
                );

                questionSubmissionRepository.save(questionSubmission);
            }

            integrationToken.getSubmission().setAlteredCalculatedGrade(calculatedScore);
            integrationToken.getSubmission().setCalculatedGrade(calculatedScore);
            integrationToken.getSubmission().setTotalAlteredGrade(calculatedScore);
            integrationToken.getSubmission().setDateSubmitted(Timestamp.from(Instant.now()));
            submissionRepository.save(integrationToken.getSubmission());

            // create a log for the token
            integrationTokenLogRepository.save(
                IntegrationTokenLog.builder()
                    .integrationToken(integrationToken)
                    .score(score)
                    .status(IntegrationTokenStatus.SUCCESS)
                    .build()
            );
        } catch (IntegrationTokenInvalidException e) {
            createErrorLog(e.getMessage(), score, launchToken, IntegrationTokenStatus.INVALID);
            throw new IntegrationTokenInvalidException(e.getMessage(), e);
        } catch (IntegrationTokenNotFoundException e) {
            createErrorLog(e.getMessage(), score, launchToken);
            throw new IntegrationTokenNotFoundException(e.getMessage(), e);
        } catch (DataServiceException e) {
            createErrorLog(e.getMessage(), score, launchToken);
            throw new DataServiceException(e.getMessage(), e);
        } catch (Exception e) {
            createErrorLog(e.getMessage(), score, launchToken);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void createErrorLog(String errorMessage, String score, String launchToken) {
        createErrorLog(errorMessage, score, launchToken, IntegrationTokenStatus.ERROR);
    }

    private void createErrorLog(String errorMessage, String score, String launchToken, IntegrationTokenStatus status) {
        integrationTokenLogRepository.save(
            IntegrationTokenLog.builder()
                .error(errorMessage)
                .score(score)
                .status(status)
                .token(launchToken)
                .build()
        );
    }

}
