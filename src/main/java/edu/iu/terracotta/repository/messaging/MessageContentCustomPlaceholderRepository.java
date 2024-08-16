package edu.iu.terracotta.repository.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageContentCustomPlaceholder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageContentCustomPlaceholderRepository extends JpaRepository<MessageContentCustomPlaceholder, Long> {

    Optional<MessageContentCustomPlaceholder> findByUuid(UUID uuid);
    List<MessageContentCustomPlaceholder> findAllByContent_Id(long contentId);

}
