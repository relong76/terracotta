package edu.iu.terracotta.repository.messaging;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageConfiguration;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageConfigurationRepository extends JpaRepository<MessageConfiguration, Long> {

    Optional<MessageConfiguration> findByUuid(UUID uuid);
    Optional<MessageConfiguration> findByMessage_Id(long messageId);
    Optional<MessageConfiguration> findByUuidAndMessage_Uuid(UUID uuid, UUID messageUuid);

}
