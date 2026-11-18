package edu.iu.terracotta.connectors.canvas.service.lti;

import java.util.Map;

/**
 * Terracotta's Canvas configuration: the launch and key URLs, scopes, placement, privacy level and
 * custom fields a Canvas developer key needs. One source for both ways Canvas can take it.
 */
public interface CanvasToolConfigurationService {

    /**
     * The JSON a Canvas admin enters by URL ("1.3 JSON URL") or by pasting it ("1.3 by Paste JSON
     * Code") when creating an LTI developer key.
     */
    Map<String, Object> developerKeyConfiguration();

    /**
     * The client registration Terracotta sends Canvas during LTI Dynamic Registration ("1.3
     * Dynamic Registration"), with Canvas's own extensions.
     */
    Map<String, Object> registrationRequest();

}
