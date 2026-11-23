package edu.iu.terracotta.connectors.generic.dao.model.lti.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import edu.iu.terracotta.utils.LtiStrings;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformConfigurationDto {

    private String product_family_code;
    private String version;
    private List<MessagesSupportedDto> messages_supported;
    private List<String> variables;

    // Canvas's own: which school (root account) is registering
    @JsonProperty(LtiStrings.CANVAS_ACCOUNT_NAME)
    private String canvasAccountName;

    // the same ID Canvas launches carry as tool_platform's guid
    @JsonProperty(LtiStrings.CANVAS_ACCOUNT_LTI_GUID)
    private String canvasAccountLtiGuid;

    @JsonProperty(LtiStrings.CANVAS_ACCOUNT_DOMAIN)
    private String canvasAccountDomain;

}
