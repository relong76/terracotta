package edu.iu.terracotta.repository.messaging;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageGroupConfiguration;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageGroupConfigurationRepository extends JpaRepository<MessageGroupConfiguration, Long> {

    Optional<MessageGroupConfiguration> findByUuid(UUID uuid);
    Optional<MessageGroupConfiguration> findByUuidAndGroup_Uuid(UUID uuid, UUID groupUuid);

}
