package edu.iu.terracotta.repository.messaging;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageLog;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageLogRepository extends JpaRepository <MessageLog, Long> {

    List<MessageLog> findAllByMessage_Id(long messageId);

}
