package edu.iu.terracotta.connectors.generic.dao.model.lti.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import edu.iu.terracotta.utils.LtiStrings;

@Getter
@AllArgsConstructor
public enum LtiAgsScope {

    AGS_LINEITEM(LtiStrings.LTI_SCOPE_AGS_LINEITEM),
    AGS_RESULT_READONLY(LtiStrings.LTI_SCOPE_AGS_RESULT_READONLY),
    AGS_SCORE(LtiStrings.LTI_SCOPE_AGS_SCORE),
    DELETE_RESULTS("deleteResults"),
    LINEITEM("lineitem"),
    LINEITEMS("lineitems"),
    LIS_LINEITEM_CONTAINER_JSON("application/vnd.ims.lis.v2.lineitemcontainer+json"),
    LIS_LINEITEM_JSON("application/vnd.ims.lis.v2.lineitem+json"),
    NRPS_MEMBERSHIP_JSON_ACCEPT("application/vnd.ims.lti-nrps.v2.membershipcontainer+json"),
    NRPS_MEMBERSHIP_READONLY(LtiStrings.LTI_SCOPE_NRPS_MEMBERSHIP_READONLY),
    RESULTS("results"),
    SCORE("score"),
    SCORES("scores"),
    SINGLE("single");

    private String key;

    public String key() {
        return key;
    }

}
