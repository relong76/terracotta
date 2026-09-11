package edu.iu.terracotta.service.app.distribute.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.jsonwebtoken.Claims;

import edu.iu.terracotta.base.BaseTest;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.LtiContextEntity;
import edu.iu.terracotta.connectors.generic.service.lti.LtiNoticeService;
import edu.iu.terracotta.dao.entity.distribute.ExperimentCopyCandidate;
import edu.iu.terracotta.dao.model.dto.distribute.CopyCandidateDto;
import edu.iu.terracotta.dao.model.dto.distribute.ExportDto;
import edu.iu.terracotta.dao.model.dto.distribute.ImportDto;
import edu.iu.terracotta.dao.model.enums.FeatureType;
import edu.iu.terracotta.dao.model.enums.distribute.ExperimentCopyCandidateStatus;
import edu.iu.terracotta.dao.repository.distribute.ExperimentCopyCandidateRepository;
import edu.iu.terracotta.exceptions.ExperimentCopyCandidateNotFoundException;
import edu.iu.terracotta.exceptions.ExperimentExportException;
import edu.iu.terracotta.exceptions.ExperimentImportException;
import edu.iu.terracotta.service.app.FeatureService;
import edu.iu.terracotta.service.app.distribute.ExperimentExportService;

class ExperimentCopyCandidateServiceImplTest extends BaseTest {

    @Mock private ExperimentCopyCandidateRepository experimentCopyCandidateRepository;
    @Mock private LtiNoticeService ltiNoticeService;
    @Mock private FeatureService featureService;
    @Mock private ExperimentExportService experimentExportService;
    @Mock private Claims noticeClaims;
    @Mock private ExperimentCopyCandidate copyCandidate;

    @InjectMocks private ExperimentCopyCandidateServiceImpl experimentCopyCandidateService;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        setup();

        when(featureService.isFeatureEnabled(eq(FeatureType.PLATFORM_NOTIFICATIONS), anyLong())).thenReturn(true);
    }

    @Test
    void testStageFromNoticeNoDestinationResolvedDoesNothing() {
        when(ltiNoticeService.resolveOrCreateContext(noticeClaims)).thenReturn(Optional.empty());

        experimentCopyCandidateService.stageFromNotice(noticeClaims);

        verify(experimentCopyCandidateRepository, never()).save(any());
    }

    @Test
    void testStageFromNoticeFeatureDisabledDoesNothing() {
        when(ltiNoticeService.resolveOrCreateContext(noticeClaims)).thenReturn(Optional.of(ltiContextEntity));
        when(featureService.isFeatureEnabled(eq(FeatureType.PLATFORM_NOTIFICATIONS), anyLong())).thenReturn(false);

        experimentCopyCandidateService.stageFromNotice(noticeClaims);

        verify(ltiNoticeService, never()).resolveOriginContexts(any());
        verify(experimentCopyCandidateRepository, never()).save(any());
    }

    @Test
    void testStageFromNoticeStagesOneCandidatePerExperimentAcrossOrigins() {
        LtiContextEntity origin1 = mock(LtiContextEntity.class);
        LtiContextEntity origin2 = mock(LtiContextEntity.class);
        when(origin1.getContextId()).thenReturn(10L);
        when(origin2.getContextId()).thenReturn(20L);

        when(ltiNoticeService.resolveOrCreateContext(noticeClaims)).thenReturn(Optional.of(ltiContextEntity));
        when(ltiNoticeService.resolveOriginContexts(noticeClaims)).thenReturn(List.of(origin1, origin2));
        when(experimentRepository.findAllByLtiContextEntity_ContextId(10L)).thenReturn(List.of(experiment));
        when(experimentRepository.findAllByLtiContextEntity_ContextId(20L)).thenReturn(List.of());
        when(experimentCopyCandidateRepository.existsBySourceExperiment_ExperimentIdAndDestinationContext_ContextId(1L, 1L)).thenReturn(false);

        experimentCopyCandidateService.stageFromNotice(noticeClaims);

        verify(experimentCopyCandidateRepository, times(1)).save(any(ExperimentCopyCandidate.class));
    }

    // PNS notices can be redelivered - staging the same (experiment, destination) pair twice
    // must not create a second candidate row
    @Test
    void testStageFromNoticeIsIdempotentOnRedelivery() {
        LtiContextEntity origin = mock(LtiContextEntity.class);
        when(origin.getContextId()).thenReturn(10L);

        when(ltiNoticeService.resolveOrCreateContext(noticeClaims)).thenReturn(Optional.of(ltiContextEntity));
        when(ltiNoticeService.resolveOriginContexts(noticeClaims)).thenReturn(List.of(origin));
        when(experimentRepository.findAllByLtiContextEntity_ContextId(10L)).thenReturn(List.of(experiment));
        when(experimentCopyCandidateRepository.existsBySourceExperiment_ExperimentIdAndDestinationContext_ContextId(1L, 1L)).thenReturn(true);

        experimentCopyCandidateService.stageFromNotice(noticeClaims);

        verify(experimentCopyCandidateRepository, never()).save(any());
    }

    @Test
    void testGetPendingForContextEmptyWhenDestinationAlreadyHasExperiments() {
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentRepository.findAllByLtiContextEntity_ContextId(1L)).thenReturn(List.of(experiment));

        List<CopyCandidateDto> result = experimentCopyCandidateService.getPendingForContext(securedInfo);

        assertTrue(result.isEmpty());
        verify(experimentCopyCandidateRepository, never()).findAllByDestinationContext_ContextIdAndStatus(anyLong(), any());
    }

    @Test
    void testGetPendingForContextReturnsCandidates() {
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentRepository.findAllByLtiContextEntity_ContextId(1L)).thenReturn(List.of());
        when(copyCandidate.getUuid()).thenReturn(UUID.randomUUID());
        when(copyCandidate.getSourceExperiment()).thenReturn(experiment);
        when(copyCandidate.getStatus()).thenReturn(ExperimentCopyCandidateStatus.PENDING);
        when(experimentCopyCandidateRepository.findAllByDestinationContext_ContextIdAndStatus(1L, ExperimentCopyCandidateStatus.PENDING))
            .thenReturn(List.of(copyCandidate));
        when(conditionRepository.countByExperiment_ExperimentId(1L)).thenReturn(2L);
        when(assignmentRepository.findByExposure_Experiment_ExperimentId(1L)).thenReturn(List.of());

        List<CopyCandidateDto> result = experimentCopyCandidateService.getPendingForContext(securedInfo);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getConditionCount());
    }

    @Test
    void testImportCandidateSuccess() throws Exception {
        UUID candidateId = UUID.randomUUID();
        File exportFile = mock(File.class);
        ExportDto exportDto = ExportDto.builder().file(exportFile).filename("export.zip").build();
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentCopyCandidateRepository.findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, 1L, ExperimentCopyCandidateStatus.PENDING))
            .thenReturn(Optional.of(copyCandidate));
        when(copyCandidate.getSourceExperiment()).thenReturn(experiment);
        when(experimentRepository.findByExperimentId(1L)).thenReturn(experiment);
        when(experimentExportService.export(experiment)).thenReturn(exportDto);
        when(experimentImportService.preprocessFromFile(exportFile, "export.zip", securedInfo)).thenReturn(importDto);
        when(importDto.getId()).thenReturn(UUID.randomUUID());

        ImportDto result = experimentCopyCandidateService.importCandidate(candidateId, securedInfo);

        assertEquals(importDto, result);
        verify(copyCandidate).setStatus(ExperimentCopyCandidateStatus.IMPORTING);
        verify(copyCandidate).setStatus(ExperimentCopyCandidateStatus.IMPORTED);
        verify(copyCandidate).setResultingImportUuid(importDto.getId());
    }

    @Test
    void testImportCandidateNotFound() {
        UUID candidateId = UUID.randomUUID();
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentCopyCandidateRepository.findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, 1L, ExperimentCopyCandidateStatus.PENDING))
            .thenReturn(Optional.empty());

        assertThrows(
            ExperimentCopyCandidateNotFoundException.class,
            () -> experimentCopyCandidateService.importCandidate(candidateId, securedInfo)
        );
    }

    // belt-and-suspenders for a race between the source experiment's deletion (which cascades
    // and should already have removed this row) and this request
    @Test
    void testImportCandidateSourceExperimentGoneDeletesCandidate() {
        UUID candidateId = UUID.randomUUID();
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentCopyCandidateRepository.findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, 1L, ExperimentCopyCandidateStatus.PENDING))
            .thenReturn(Optional.of(copyCandidate));
        when(copyCandidate.getSourceExperiment()).thenReturn(experiment);
        when(experimentRepository.findByExperimentId(1L)).thenReturn(null);

        assertThrows(
            ExperimentCopyCandidateNotFoundException.class,
            () -> experimentCopyCandidateService.importCandidate(candidateId, securedInfo)
        );

        verify(experimentCopyCandidateRepository).delete(copyCandidate);
    }

    @Test
    void testImportCandidateExportFailureSetsErrorStatus() throws Exception {
        UUID candidateId = UUID.randomUUID();
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentCopyCandidateRepository.findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, 1L, ExperimentCopyCandidateStatus.PENDING))
            .thenReturn(Optional.of(copyCandidate));
        when(copyCandidate.getSourceExperiment()).thenReturn(experiment);
        when(experimentRepository.findByExperimentId(1L)).thenReturn(experiment);
        doThrow(new ExperimentExportException("export failed")).when(experimentExportService).export(experiment);

        assertThrows(
            ExperimentImportException.class,
            () -> experimentCopyCandidateService.importCandidate(candidateId, securedInfo)
        );

        verify(copyCandidate).setStatus(ExperimentCopyCandidateStatus.ERROR);
    }

    @Test
    void testDismissSuccess() throws Exception {
        UUID candidateId = UUID.randomUUID();
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentCopyCandidateRepository.findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, 1L, ExperimentCopyCandidateStatus.PENDING))
            .thenReturn(Optional.of(copyCandidate));

        experimentCopyCandidateService.dismiss(candidateId, securedInfo);

        verify(copyCandidate).setStatus(ExperimentCopyCandidateStatus.DISMISSED);
        verify(experimentCopyCandidateRepository).save(copyCandidate);
    }

    @Test
    void testDismissNotFound() {
        UUID candidateId = UUID.randomUUID();
        when(securedInfo.getContextId()).thenReturn(1L);
        when(experimentCopyCandidateRepository.findByUuidAndDestinationContext_ContextIdAndStatus(candidateId, 1L, ExperimentCopyCandidateStatus.PENDING))
            .thenReturn(Optional.empty());

        assertThrows(
            ExperimentCopyCandidateNotFoundException.class,
            () -> experimentCopyCandidateService.dismiss(candidateId, securedInfo)
        );
    }

}
