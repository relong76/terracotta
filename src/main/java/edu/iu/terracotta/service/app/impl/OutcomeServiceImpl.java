package edu.iu.terracotta.service.app.impl;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.ParticipantNotUpdatedException;
import edu.iu.terracotta.model.app.Experiment;
import edu.iu.terracotta.model.app.Exposure;
import edu.iu.terracotta.model.app.Outcome;
import edu.iu.terracotta.model.app.OutcomeScore;
import edu.iu.terracotta.model.app.Participant;
import edu.iu.terracotta.model.app.dto.OutcomeDto;
import edu.iu.terracotta.model.app.dto.OutcomePotentialDto;
import edu.iu.terracotta.model.app.dto.OutcomeScoreDto;
import edu.iu.terracotta.model.app.enumerator.LmsType;
import edu.iu.terracotta.model.canvas.AssignmentExtended;
import edu.iu.terracotta.model.oauth2.SecurityInfo;
import edu.iu.terracotta.repository.AllRepositories;
import edu.iu.terracotta.service.app.ExperimentService;
import edu.iu.terracotta.service.app.ExposureService;
import edu.iu.terracotta.service.app.OutcomeScoreService;
import edu.iu.terracotta.service.app.OutcomeService;
import edu.iu.terracotta.service.app.ParticipantService;
import edu.iu.terracotta.service.canvas.CanvasAPIClient;
import edu.ksu.canvas.model.assignment.Submission;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OutcomeServiceImpl implements OutcomeService {

    @Autowired
    AllRepositories allRepositories;

    @Autowired
    OutcomeScoreService outcomeScoreService;

    @Autowired
    ParticipantService participantService;

    @Autowired
    ExposureService exposureService;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    CanvasAPIClient canvasAPIClient;

    @Value("${application.url}")
    private String localUrl;

    @Override
    public List<Outcome> findAllByExposureId(Long exposureId) {
        return allRepositories.outcomeRepository.findByExposure_ExposureId(exposureId);
    }

    @Override
    public OutcomeDto toDto(Outcome outcome, boolean outcomeScores) {
        OutcomeDto outcomeDto = new OutcomeDto();
        outcomeDto.setOutcomeId(outcome.getOutcomeId());
        outcomeDto.setExposureId(outcome.getExposure().getExposureId());
        outcomeDto.setTitle(outcome.getTitle());
        outcomeDto.setLmsType(outcome.getLmsType().name());
        outcomeDto.setLmsOutcomeId(outcome.getLmsOutcomeId());
        outcomeDto.setMaxPoints(outcome.getMaxPoints());
        outcomeDto.setExternal(outcome.getExternal());
        List<OutcomeScoreDto> outcomeScoreDtoList = new ArrayList<>();
        if(outcomeScores) {
            List<OutcomeScore> outcomeScoreList = allRepositories.outcomeScoreRepository.findByOutcome_OutcomeId(outcome.getOutcomeId());
            for(OutcomeScore outcomeScore : outcomeScoreList) {
                outcomeScoreDtoList.add(outcomeScoreService.toDto(outcomeScore));
            }
        }
        outcomeDto.setOutcomeScoreDtoList(outcomeScoreDtoList);

        return outcomeDto;
    }

    @Override
    public Outcome fromDto(OutcomeDto outcomeDto) throws DataServiceException{
        Outcome outcome = new Outcome();
        outcome.setOutcomeId(outcomeDto.getOutcomeId());
        outcome.setTitle(outcomeDto.getTitle());
        outcome.setLmsType(EnumUtils.getEnum(LmsType.class, outcomeDto.getLmsType(), LmsType.none));
        outcome.setMaxPoints(outcomeDto.getMaxPoints());
        outcome.setLmsOutcomeId(outcomeDto.getLmsOutcomeId());
        outcome.setExternal(outcomeDto.getExternal());
        Optional<Exposure> exposure = exposureService.findById(outcomeDto.getExposureId());
        if(exposure.isPresent()){
            outcome.setExposure(exposure.get());
        } else{
            throw new DataServiceException("Exposure for outcome does not exist.");
        }

        return outcome;
    }

    @Override
    public Outcome save(Outcome outcome) { return allRepositories.outcomeRepository.save(outcome); }

    @Override
    public Optional<Outcome> findById(Long id) { return allRepositories.outcomeRepository.findById(id); }

    @Override
    public void saveAndFlush(Outcome outcomeToChange) { allRepositories.outcomeRepository.saveAndFlush(outcomeToChange); }

    @Override
    public void deleteById(Long id) { allRepositories.outcomeRepository.deleteByOutcomeId(id); }

    @Override
    public boolean outcomeBelongsToExperimentAndExposure(Long experimentId, Long exposureId, Long outcomeId){
        return allRepositories.outcomeRepository.existsByExposure_Experiment_ExperimentIdAndExposure_ExposureIdAndOutcomeId(experimentId, exposureId, outcomeId);
    }

    @Override
    public List<OutcomePotentialDto> potentialOutcomes(Long experimentId) throws DataServiceException, CanvasApiException {
        Optional<Experiment> experiment = experimentService.findById(experimentId);
        List<OutcomePotentialDto> outcomePotentialDtos = new ArrayList<>();
        if (experiment.isPresent()) {
            List<AssignmentExtended> assignmentExtendedList = canvasAPIClient.listAssignments(experiment.get().getLtiContextEntity().getContext_memberships_url(),experiment.get().getPlatformDeployment());
            for (AssignmentExtended assignmentExtended:assignmentExtendedList){
                    outcomePotentialDtos.add(assignmentExtendedToOutcomePotentialDto(assignmentExtended));
            }
        } else {
            throw new DataServiceException("Experiment does not exist.");
        }
        return outcomePotentialDtos;
    }

    private OutcomePotentialDto assignmentExtendedToOutcomePotentialDto(AssignmentExtended assignmentExtended){
        OutcomePotentialDto potentialDto = new OutcomePotentialDto();
        potentialDto.setAssignmentId(assignmentExtended.getId());
        potentialDto.setName(assignmentExtended.getName());
        potentialDto.setType(assignmentExtended.getSubmissionTypes().get(0));
        potentialDto.setPointsPossible(assignmentExtended.getPointsPossible());
        potentialDto.setTerracotta(assignmentExtended.getSubmissionTypes().contains("external_tool") && assignmentExtended.getExternalToolTagAttributes().getUrl().contains(localUrl));
        return potentialDto;
    }

    @Override
    @Transactional
    public void updateOutcomeGrades(Long outcomeId, SecurityInfo securityInfo) throws CanvasApiException, IOException, ParticipantNotUpdatedException {
        Optional<Outcome> outcomeSearchResult = this.findById(outcomeId);
        Outcome outcome = outcomeSearchResult.get();
        //If this is not external we don't need to check the scores.
        if (!outcome.getExternal()){
            return;
        }
        participantService.refreshParticipants(outcome.getExposure().getExperiment().getExperimentId(),securityInfo,outcome.getExposure().getExperiment().getParticipants());
        List<OutcomeScore> newScores = new ArrayList<>();
        List<Submission> submissions = canvasAPIClient.listSubmissions(Integer.parseInt(outcome.getLmsOutcomeId()),outcome.getExposure().getExperiment().getLtiContextEntity().getContext_memberships_url(),outcome.getExposure().getExperiment().getPlatformDeployment());
        for (Submission submission:submissions){
            boolean found = false;
            for (OutcomeScore outcomeScore:outcome.getOutcomeScores()){
                if (outcomeScore.getParticipant().getLtiUserEntity().getEmail() != null && outcomeScore.getParticipant().getLtiUserEntity().getEmail().equals(submission.getUser().getLoginId()) && outcomeScore.getParticipant().getLtiUserEntity().getDisplayName().equals(submission.getUser().getName())) {
                    found = true;
                    if (submission.getScore()!=null) {
                        outcomeScore.setScoreNumeric(submission.getScore().floatValue());
                    } else {
                        outcomeScore.setScoreNumeric(null);
                    }
                    break;
                }
            }
            if (!found) {
                for (OutcomeScore outcomeScore:outcome.getOutcomeScores()){
                    if (outcomeScore.getParticipant().getLtiUserEntity().getDisplayName().equals(submission.getUser().getName())) {
                        found = true;
                        if (submission.getScore()!=null) {
                            outcomeScore.setScoreNumeric(submission.getScore().floatValue());
                        } else {
                            outcomeScore.setScoreNumeric(null);
                        }
                        break;
                    }

                }
            }
            if (!found) {
                for (Participant participant:outcome.getExposure().getExperiment().getParticipants()){
                    if (participant.getLtiUserEntity().getEmail() != null && participant.getLtiUserEntity().getEmail().equals(submission.getUser().getLoginId()) && participant.getLtiUserEntity().getDisplayName().equals(submission.getUser().getName())) {
                        found = true;
                        OutcomeScore outcomeScore = new OutcomeScore();
                        outcomeScore.setOutcome(outcome);
                        outcomeScore.setParticipant(participant);
                        if (submission.getScore()!=null) {
                            outcomeScore.setScoreNumeric(submission.getScore().floatValue());
                        } else {
                            outcomeScore.setScoreNumeric(null);
                        }
                        newScores.add(outcomeScore);
                        break;
                    }
                }
            }
            if (!found) {
                for (Participant participant:outcome.getExposure().getExperiment().getParticipants()){
                    if (participant.getLtiUserEntity().getDisplayName().equals(submission.getUser().getName())) {
                        OutcomeScore outcomeScore = new OutcomeScore();
                        outcomeScore.setOutcome(outcome);
                        outcomeScore.setParticipant(participant);
                        if (submission.getScore()!=null) {
                            outcomeScore.setScoreNumeric(submission.getScore().floatValue());
                        } else {
                            outcomeScore.setScoreNumeric(null);
                        }
                        newScores.add(outcomeScore);
                        break;
                    }
                }
            }

        }

        for (OutcomeScore outcomeScore:newScores){
            outcomeScoreService.save(outcomeScore);
        }
        //TODO, what to do if the outcome score is there but the participant is dropped.

    }

}