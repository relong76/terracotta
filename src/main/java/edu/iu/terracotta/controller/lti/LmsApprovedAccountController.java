package edu.iu.terracotta.controller.lti;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.LmsApprovedAccountDto;
import edu.iu.terracotta.connectors.generic.service.lti.LmsApprovedAccountService;
import lombok.RequiredArgsConstructor;

/**
 * The admin API for LMS accounts (see LmsApprovedAccount), behind the same admin login as the rest
 * of /config. Approve a PENDING one by setting its status to APPROVED, or turn it down with
 * REJECTED; REVOKED stops an approved one's launches. There's no delete: an account's platform
 * deployments would then launch unchecked.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/config/lms-approved-accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class LmsApprovedAccountController {

    private final LmsApprovedAccountService lmsApprovedAccountService;

    @GetMapping
    public List<LmsApprovedAccountDto> getAll() {
        return lmsApprovedAccountService.getAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> create(@RequestBody LmsApprovedAccountDto lmsApprovedAccountDto, Principal principal) {
        try {
            return new ResponseEntity<>(lmsApprovedAccountService.create(lmsApprovedAccountDto, adminName(principal)), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/{uuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(@PathVariable UUID uuid, @RequestBody LmsApprovedAccountDto lmsApprovedAccountDto, Principal principal) {
        try {
            return lmsApprovedAccountService.update(uuid, lmsApprovedAccountDto, adminName(principal))
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(Map.of("error", "No LMS account with that ID."), HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private static String adminName(Principal principal) {
        return principal != null ? principal.getName() : null;
    }

}
