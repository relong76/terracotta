package edu.iu.terracotta.repository.messaging;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageContentStandardPlaceholder;

public interface MessageContentStandardPlaceholderRepository extends JpaRepository<MessageContentStandardPlaceholder, Long> {

    Optional<MessageContentStandardPlaceholder> findByUuid(UUID uuid);
    List<MessageContentStandardPlaceholder> findAllByEnabled(boolean enabled);

}
