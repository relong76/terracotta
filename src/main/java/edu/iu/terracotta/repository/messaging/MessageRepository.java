package edu.iu.terracotta.repository.messaging;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.Message;
import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByUuid(UUID uuid);
    // List<Message> findAllByExposureGroupCondition_Exposure_Experiment_ExperimentIdAndGroup_Owner_UserId(long experimentId, long ownerId);
    boolean existsByUuidAndGroup_UuidAndGroup_Owner_LmsUserId(UUID messageUuid, UUID groupUuid, String lmsUserId);
    List<Message> findByConfiguration_StatusAndConfiguration_SendAtLessThan(MessageStatus messageStatus, Timestamp sendAt);
    //boolean existsByExposureGroupCondition_ExposureGroupConditionId(long exposureGroupConditionId);

}
