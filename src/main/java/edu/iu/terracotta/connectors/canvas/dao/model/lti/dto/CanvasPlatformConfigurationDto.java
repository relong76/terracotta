package edu.iu.terracotta.connectors.canvas.dao.model.lti.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.iu.terracotta.connectors.canvas.utils.CanvasLtiStrings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Canvas's own values in a platform configuration (the lti-platform-configuration object of its
 * OpenID configuration): which school (root account) is registering. The standard values are in
 * PlatformConfigurationDto.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasPlatformConfigurationDto {

    @JsonProperty(CanvasLtiStrings.ACCOUNT_NAME)
    private String accountName;

    // the same ID Canvas launches carry as tool_platform's guid
    @JsonProperty(CanvasLtiStrings.ACCOUNT_LTI_GUID)
    private String accountLtiGuid;

    @JsonProperty(CanvasLtiStrings.ACCOUNT_DOMAIN)
    private String accountDomain;

}
