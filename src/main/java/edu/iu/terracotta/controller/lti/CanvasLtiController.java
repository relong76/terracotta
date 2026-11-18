package edu.iu.terracotta.controller.lti;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.iu.terracotta.connectors.canvas.service.lti.CanvasDynamicRegistrationService;
import edu.iu.terracotta.connectors.canvas.service.lti.CanvasRegistrationException;
import edu.iu.terracotta.connectors.canvas.service.lti.CanvasToolConfigurationService;
import lombok.RequiredArgsConstructor;

/**
 * The two ways a Canvas admin can add Terracotta without typing its configuration in: a JSON URL
 * for a new developer key, and LTI Dynamic Registration (approved Canvas instances only).
 * Neither needs a Terracotta login: Canvas calls them before Terracotta knows the Canvas.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/lti/canvas")
public class CanvasLtiController {

    public static final String REGISTRATION_TEMPLATE = "canvasRegistration";

    private final CanvasToolConfigurationService canvasToolConfigurationService;
    private final CanvasDynamicRegistrationService canvasDynamicRegistrationService;

    /**
     * "1.3 JSON URL": Terracotta's developer key configuration. Public, and holds nothing secret.
     */
    @ResponseBody
    @GetMapping(value = "/config.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> configuration() {
        return canvasToolConfigurationService.developerKeyConfiguration();
    }

    /**
     * "1.3 Dynamic Registration": Canvas opens this with openid_configuration and registration_token.
     * It registers straight away, rather than after a second request: Canvas shows it in a frame,
     * where browsers may block the session cookie a second request would need.
     */
    @GetMapping("/registration")
    public String registration(
            @RequestParam(name = "openid_configuration", required = false) String openidConfiguration,
            @RequestParam(name = "registration_token", required = false) String registrationToken,
            Model model) {
        try {
            canvasDynamicRegistrationService.register(openidConfiguration, registrationToken);
            model.addAttribute("registered", true);
        } catch (CanvasRegistrationException e) {
            model.addAttribute("registered", false);
            model.addAttribute("errorMessage", e.getMessage());
        }

        return REGISTRATION_TEMPLATE;
    }

}
