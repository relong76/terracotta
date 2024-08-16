package edu.iu.terracotta.repository.messaging;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageGroup;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageGroupRepository extends JpaRepository<MessageGroup, Long> {

    Optional<MessageGroup> findByUuid(UUID uuid);
    List<MessageGroup> findAllByExposure_ExposureIdAndOwner_LmsUserId(long exposureId, String lmsUserId);
    List<MessageGroup> findAllByExposure_Experiment_ExperimentIdAndExposure_ExposureIdAndOwner_LmsUserId(long experimentId, long exposureId, String lmsUserId);
    boolean existsByUuidAndOwner_LmsUserId(UUID messageUuid, String lmsUserId);
    boolean existsByExposure_ExposureId(long exposureId);

}
