package edu.iu.terracotta.model.app.messaging.dto;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageDto {

    private UUID id;
    private UUID groupId;
    private MessageConfigurationDto configuration;
    private MessageContentDto content;
    private Timestamp created;
    private long exposureGroupConditionId;
    private long conditionId;
    private String ownerEmail;

}
