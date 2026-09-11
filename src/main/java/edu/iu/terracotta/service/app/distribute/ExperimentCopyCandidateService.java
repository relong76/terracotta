package edu.iu.terracotta.service.app.distribute;

import java.util.List;
import java.util.UUID;

import io.jsonwebtoken.Claims;

import edu.iu.terracotta.connectors.generic.dao.model.SecuredInfo;
import edu.iu.terracotta.dao.model.dto.distribute.CopyCandidateDto;
import edu.iu.terracotta.dao.model.dto.distribute.ImportDto;
import edu.iu.terracotta.exceptions.ExperimentCopyCandidateNotFoundException;
import edu.iu.terracotta.exceptions.ExperimentImportException;

/**
 * Detects (via an LTI Advantage Platform Notification Service "course copy" notice) that a course
 * was copied from another course Terracotta has an Experiment in, stages that as a pending
 * ExperimentCopyCandidate, and - once the instructor picks one during a live launch into the
 * destination course - drives the existing export/import pipeline to recreate it there.
 */
public interface ExperimentCopyCandidateService {

    /**
     * Called at notice-time (no live session) - resolves/creates the destination context and the
     * origin context(s) named in the notice, and stages a PENDING candidate for every Experiment
     * found in those origin context(s) that doesn't already have one. Idempotent: safe to call
     * more than once for the same notice (PNS notices can be redelivered).
     */
    void stageFromNotice(Claims noticeClaims);

    /**
     * Pending candidates for the current (live-launch) context - always empty once that context
     * already has any Experiment of its own, matching the same "is this course new" signal the
     * rest of the app already uses to gate the first-launch/zero-state experience.
     */
    List<CopyCandidateDto> getPendingForContext(SecuredInfo securedInfo);

    /**
     * Recreates the candidate's source Experiment into the current (live-launch) context, by
     * exporting it (ExperimentExportService, unmodified) and feeding that export straight into
     * the existing import pipeline (ExperimentImportService, unmodified) - the same async
     * import/poll contract a manual zip upload already has.
     */
    ImportDto importCandidate(UUID candidateId, SecuredInfo securedInfo) throws ExperimentCopyCandidateNotFoundException, ExperimentImportException;

    void dismiss(UUID candidateId, SecuredInfo securedInfo) throws ExperimentCopyCandidateNotFoundException;

}
