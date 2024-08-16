package edu.iu.terracotta.repository.messaging;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageContent;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageContentRepository extends JpaRepository<MessageContent, Long> {

    Optional<MessageContent> findByUuid(UUID uuid);
    Optional<MessageContent> findByUuidAndMessage_Uuid(UUID uuid, UUID messageUuid);

}
