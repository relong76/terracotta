package edu.iu.terracotta.service.app.distribute.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LtiContextEntity;
import edu.iu.terracotta.connectors.generic.dao.model.SecuredInfo;
import edu.iu.terracotta.connectors.generic.service.lti.LtiNoticeService;
import edu.iu.terracotta.dao.entity.Experiment;
import edu.iu.terracotta.dao.entity.distribute.ExperimentCopyCandidate;
import edu.iu.terracotta.dao.model.dto.distribute.CopyCandidateDto;
import edu.iu.terracotta.dao.model.dto.distribute.ExportDto;
import edu.iu.terracotta.dao.model.dto.distribute.ImportDto;
import edu.iu.terracotta.dao.model.enums.FeatureType;
import edu.iu.terracotta.dao.model.enums.distribute.ExperimentCopyCandidateStatus;
import edu.iu.terracotta.dao.repository.AssignmentRepository;
import edu.iu.terracotta.dao.repository.ConditionRepository;
import edu.iu.terracotta.dao.repository.ExperimentRepository;
import edu.iu.terracotta.dao.repository.distribute.ExperimentCopyCandidateRepository;
import edu.iu.terracotta.exceptions.ExperimentCopyCandidateNotFoundException;
import edu.iu.terracotta.exceptions.ExperimentExportException;
import edu.iu.terracotta.exceptions.ExperimentImportException;
import edu.iu.terracotta.service.app.FeatureService;
import edu.iu.terracotta.service.app.distribute.ExperimentCopyCandidateService;
import edu.iu.terracotta.service.app.distribute.ExperimentExportService;
import edu.iu.terracotta.service.app.distribute.ExperimentImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExperimentCopyCandidateServiceImpl implements ExperimentCopyCandidateService {

    private final ExperimentCopyCandidateRepository experimentCopyCandidateRepository;
    private final ExperimentRepository experimentRepository;
    private final ConditionRepository conditionRepository;
    private final AssignmentRepository assignmentRepository;
    private final LtiNoticeService ltiNoticeService;
    private final FeatureService featureService;
    private final ExperimentExportService experimentExportService;
    private final ExperimentImportService experimentImportService;

    @Override
    public void stageFromNotice(Claims noticeClaims) {
        LtiContextEntity destination = ltiNoticeService.resolveOrCreateContext(noticeClaims).orElse(null);

        if (destination == null) {
            log.warn("Could not resolve or create a destination context for a course-copy notice");
            return;
        }

        long platformDeploymentKeyId = destination.getToolDeployment().getPlatformDeployment().getKeyId();

        if (!featureService.isFeatureEnabled(FeatureType.PLATFORM_NOTIFICATIONS, platformDeploymentKeyId)) {
            return;
        }

        List<LtiContextEntity> origins = ltiNoticeService.resolveOriginContexts(noticeClaims);

        origins.stream()
            .flatMap(origin -> experimentRepository.findAllByLtiContextEntity_ContextId(origin.getContextId()).stream())
            .filter(experiment -> !experimentCopyCandidateRepository.existsBySourceExperiment_ExperimentIdAndDestinationContext_ContextId(experiment.getExperimentId(), destination.getContextId()))
            .forEach(experiment ->
                experimentCopyCandidateRepository.save(
                    ExperimentCopyCandidate.builder()
                        .sourceExperiment(experiment)
                        .destinationContext(destination)
                        .status(ExperimentCopyCandidateStatus.PENDING)
                        .build()
                )
            );
    }

    @Override
    public List<CopyCandidateDto> getPendingForContext(SecuredInfo securedInfo) {
        if (!experimentRepository.findAllByLtiContextEntity_ContextId(securedInfo.getContextId()).isEmpty()) {
            // this course already has at least one Experiment of its own by the time of this
            // live launch - don't surface candidates, matching the same "is this course new"
            // gating the rest of the app already applies to the zero-state experience
            return List.of();
        }

        return experimentCopyCandidateRepository.findAllByDestinationContext_ContextIdAndStatus(securedInfo.getContextId(), ExperimentCopyCandidateStatus.PENDING).stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    public ImportDto importCandidate(UUID candidateId, SecuredInfo securedInfo) throws ExperimentCopyCandidateNotFoundException, ExperimentImportException {
        ExperimentCopyCandidate candidate = experimentCopyCandidateRepository
            .findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, securedInfo.getContextId(), ExperimentCopyCandidateStatus.PENDING)
            .orElseThrow(() -> new ExperimentCopyCandidateNotFoundException(String.format("Experiment copy candidate with ID: [%s] not found", candidateId)));

        Experiment sourceExperiment = experimentRepository.findByExperimentId(candidate.getSourceExperiment().getExperimentId());

        if (sourceExperiment == null) {
            // belt-and-suspenders: the source_experiment_id FK's ON DELETE CASCADE should already
            // have removed this row when the source Experiment was deleted - this only covers a
            // race between that deletion and this request
            experimentCopyCandidateRepository.delete(candidate);
            throw new ExperimentCopyCandidateNotFoundException(String.format("Experiment copy candidate with ID: [%s] no longer has a source experiment", candidateId));
        }

        candidate.setStatus(ExperimentCopyCandidateStatus.IMPORTING);
        experimentCopyCandidateRepository.save(candidate);

        try {
            ExportDto exportDto = experimentExportService.export(sourceExperiment);
            ImportDto importDto = experimentImportService.preprocessFromFile(exportDto.getFile(), exportDto.getFilename(), securedInfo);

            candidate.setStatus(ExperimentCopyCandidateStatus.IMPORTED);
            candidate.setResultingImportUuid(importDto.getId());
            experimentCopyCandidateRepository.save(candidate);

            return importDto;
        } catch (ExperimentExportException | ExperimentImportException e) {
            candidate.setStatus(ExperimentCopyCandidateStatus.ERROR);
            experimentCopyCandidateRepository.save(candidate);

            throw new ExperimentImportException(String.format("Error recreating experiment from copy candidate ID: [%s]", candidateId), e);
        }
    }

    @Override
    public void dismiss(UUID candidateId, SecuredInfo securedInfo) throws ExperimentCopyCandidateNotFoundException {
        ExperimentCopyCandidate candidate = experimentCopyCandidateRepository
            .findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, securedInfo.getContextId(), ExperimentCopyCandidateStatus.PENDING)
            .orElseThrow(() -> new ExperimentCopyCandidateNotFoundException(String.format("Experiment copy candidate with ID: [%s] not found", candidateId)));

        candidate.setStatus(ExperimentCopyCandidateStatus.DISMISSED);
        experimentCopyCandidateRepository.save(candidate);
    }

    private CopyCandidateDto toDto(ExperimentCopyCandidate candidate) {
        Experiment experiment = candidate.getSourceExperiment();

        return CopyCandidateDto.builder()
            .id(candidate.getUuid())
            .sourceExperimentId(experiment.getExperimentId())
            .experimentTitle(experiment.getTitle())
            .sourceCourseTitle(experiment.getLtiContextEntity().getTitle())
            .conditionCount(Math.toIntExact(conditionRepository.countByExperiment_ExperimentId(experiment.getExperimentId())))
            .assignmentCount(assignmentRepository.findByExposure_Experiment_ExperimentId(experiment.getExperimentId()).size())
            .status(candidate.getStatus())
            .build();
    }

}
