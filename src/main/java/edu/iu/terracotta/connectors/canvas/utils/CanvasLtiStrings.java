package edu.iu.terracotta.connectors.canvas.utils;

import lombok.experimental.UtilityClass;

/**
 * Canvas's own LTI extensions: claims, scopes and values only Canvas uses. The LTI standard's
 * values are in LtiStrings.
 */
@UtilityClass
public final class CanvasLtiStrings {

    // Canvas's extension on an AGS score (e.g. new_submission, submitted_at)
    public static final String SUBMISSION = "https://canvas.instructure.com/lti/submission";

    // the platform configuration's product_family_code
    public static final String PRODUCT_FAMILY_CODE = "canvas";
    public static final String PLATFORM = "canvas.instructure.com";
    public static final String PLACEMENT_COURSE_NAVIGATION = "course_navigation";
    public static final String PRIVACY_LEVEL = "https://canvas.instructure.com/lti/privacy_level";
    public static final String PRIVACY_LEVEL_PUBLIC = "public";
    public static final String SCOPE_ACCOUNT_LOOKUP = "https://canvas.instructure.com/lti/account_lookup/scope/show";

    // in Canvas's platform configuration: which school (root account) is registering
    public static final String ACCOUNT_NAME = "https://canvas.instructure.com/lti/account_name";
    public static final String ACCOUNT_LTI_GUID = "https://canvas.instructure.com/lti/account_lti_guid";
    public static final String ACCOUNT_DOMAIN = "https://canvas.instructure.com/lti/account_domain";

}
