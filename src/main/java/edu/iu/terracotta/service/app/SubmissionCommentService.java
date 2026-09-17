package edu.iu.terracotta.service.app;

import edu.iu.terracotta.connectors.generic.dao.model.SecuredInfo;
import edu.iu.terracotta.dao.entity.SubmissionComment;
import edu.iu.terracotta.dao.model.dto.SubmissionCommentDto;
import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.IdInPostException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public interface SubmissionCommentService {

    List<SubmissionCommentDto> getSubmissionComments(Long submissionId);
    SubmissionComment getSubmissionComment(Long id);
    // fetches and maps within a single transaction - see AssessmentService.getAssessmentDto for why
    // calling getX(id) then toDto(...) as two separate top-level calls isn't safe once open-in-view is disabled
    SubmissionCommentDto getSubmissionCommentDto(Long id);
    SubmissionCommentDto postSubmissionComment(SubmissionCommentDto submissionCommentDto, long submissionId, SecuredInfo securedInfo) throws IdInPostException, DataServiceException;
    void updateSubmissionComment(SubmissionComment submissionComment, SubmissionCommentDto submissionCommentDto);
    SubmissionCommentDto toDto(SubmissionComment submissionComment);
    SubmissionComment fromDto(SubmissionCommentDto submissionCommentDto) throws DataServiceException;
    void deleteById(Long id) throws EmptyResultDataAccessException;
    HttpHeaders buildHeaders(UriComponentsBuilder ucBuilder, long experimentId, long conditionId, long treatmentId, long assessmentId, long submissionId, long submissionCommentId);

}
