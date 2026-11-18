package edu.iu.terracotta.connectors.generic.dao.model.lti.dto;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsApprovedAccountStatus;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An LmsApprovedAccount in the admin API (/config/lms-approved-accounts). On a create or update,
 * only name, lmsConnector, status and hosts are read; a create without a status is PENDING.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LmsApprovedAccountDto {

    private UUID uuid;
    private String name;
    private LmsConnector lmsConnector;
    private LmsApprovedAccountStatus status;
    private String platformGuid;
    private String statusUpdatedBy;
    private Timestamp statusUpdatedAt;
    private List<String> hosts;

}
