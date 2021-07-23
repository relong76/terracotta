package edu.iu.terracotta.service.app;

import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.MultipleChoiceLimitReachedException;
import edu.iu.terracotta.model.app.AnswerMc;
import edu.iu.terracotta.model.app.dto.AnswerDto;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AnswerService {

    //METHODS FOR MC ANSWERS
    List<AnswerDto> findAllByQuestionIdMC(Long questionId, boolean student);

    AnswerDto getAnswerMC(Long answerId, boolean student);

    AnswerDto toDtoMC(AnswerMc answer, boolean student);

    AnswerMc fromDtoMC(AnswerDto answerDto) throws DataServiceException;

    AnswerMc saveMC(AnswerMc answer);

    AnswerMc findByAnswerId(Long answerId);

    void updateAnswerMC(Map<AnswerMc, AnswerDto> map);

    void deleteByIdMC(Long id) throws EmptyResultDataAccessException;

    boolean mcAnswerBelongsToQuestionAndAssessment(Long assessmentId, Long questionId, Long answerId);

    HttpHeaders buildHeaders(UriComponentsBuilder ucBuilder, Long experimentId, Long conditionId, Long treatmentId, Long assessmentId, Long questionId, Long answerId);

    void limitReached(Long questionId) throws MultipleChoiceLimitReachedException;


    //METHODS FOR ALL ANSWER TYPES
    String getQuestionType(Long questionId);
}
