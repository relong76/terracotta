package edu.iu.terracotta.repository.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageEmailReplyTo;

public interface MessageEmailReplyToRepository extends JpaRepository<MessageEmailReplyTo, Long> {

}
