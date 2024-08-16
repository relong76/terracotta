package edu.iu.terracotta.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.iu.terracotta.model.app.scheduledtask.ScheduledTask;
import edu.iu.terracotta.model.app.scheduledtask.ScheduledTaskId;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, ScheduledTaskId> {

    Optional<ScheduledTask> findByTaskName(String taskName);

}
