package edu.iu.terracotta.service.app;

import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.QuestionSubmissionComment;
import edu.iu.terracotta.model.app.dto.QuestionSubmissionCommentDto;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

public interface QuestionSubmissionCommentService {

    List<QuestionSubmissionComment> findAllByQuestionSubmissionId(Long questionSubmissionId);

    QuestionSubmissionCommentDto toDto(QuestionSubmissionComment questionSubmissionComment);

    QuestionSubmissionComment fromDto(QuestionSubmissionCommentDto questionSubmissionCommentDto) throws DataServiceException;

    QuestionSubmissionComment save(QuestionSubmissionComment questionSubmissionComment);

    Optional<QuestionSubmissionComment> findById(Long id);

    LtiUserEntity findByUserKey(String key);

    void saveAndFlush(QuestionSubmissionComment questionSubmissionCommentToChange);

    void deleteById(Long id) throws EmptyResultDataAccessException;

    boolean questionSubmissionCommentBelongsToQuestionSubmission(Long questionSubmissionId, Long questionSubmissionCommentId);
}