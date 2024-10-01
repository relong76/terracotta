package edu.iu.terracotta.controller.app.integrations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenInvalidException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenNotFoundException;
import edu.iu.terracotta.service.app.integrations.IntegrationScoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/integrations")
@SuppressWarnings({"PMD.GuardLogStatement"})
public class IntegrationsController {

    @Autowired private IntegrationScoreService integrationScoreService;

    @GetMapping
    public String score(@RequestParam(name = "launch_token") String launchToken, @RequestParam String score, HttpServletRequest req) {
        log.info("launch_token: [{}], score: [{}]", launchToken, score);
        HttpStatus status = HttpStatus.OK;

        try {
            integrationScoreService.score(launchToken, score);
        } catch (IntegrationTokenNotFoundException e) {
            log.error(e.getMessage(), e);
            status = HttpStatus.NOT_FOUND;
        } catch (IntegrationTokenInvalidException | DataServiceException | RuntimeException e) {
            log.error(e.getMessage(), e);
            status = HttpStatus.BAD_REQUEST;
        }

        return String.format("redirect:/app/app.html?integration=1&status=%s", status.name());
    }

}
