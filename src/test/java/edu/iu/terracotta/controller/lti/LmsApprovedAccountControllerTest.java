package edu.iu.terracotta.controller.lti;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsApprovedAccountStatus;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.LmsApprovedAccountDto;
import edu.iu.terracotta.connectors.generic.service.lti.LmsApprovedAccountService;

public class LmsApprovedAccountControllerTest {

    private static final Principal ADMIN = () -> "admin";
    private static final String BODY = """
        { "name": "Indiana University", "lmsConnector": "CANVAS", "status": "APPROVED", "hosts": ["iu.instructure.com", "canvas.iu.edu"] }
        """;

    private LmsApprovedAccountService lmsApprovedAccountService;
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        lmsApprovedAccountService = mock(LmsApprovedAccountService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LmsApprovedAccountController(lmsApprovedAccountService)).build();
    }

    private static LmsApprovedAccountDto iu() {
        return LmsApprovedAccountDto.builder()
            .uuid(UUID.randomUUID())
            .name("Indiana University")
            .lmsConnector(LmsConnector.CANVAS)
            .status(LmsApprovedAccountStatus.APPROVED)
            .statusUpdatedBy("admin")
            .hosts(List.of("iu.instructure.com", "canvas.iu.edu"))
            .build();
    }

    @Test
    public void testListsTheAccounts() throws Exception {
        when(lmsApprovedAccountService.getAll()).thenReturn(List.of(iu()));

        mockMvc.perform(get("/config/lms-approved-accounts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Indiana University"))
            .andExpect(jsonPath("$[0].lmsConnector").value("CANVAS"))
            .andExpect(jsonPath("$[0].status").value("APPROVED"))
            .andExpect(jsonPath("$[0].hosts[1]").value("canvas.iu.edu"));
    }

    @Test
    public void testCreatesAnAccountAsTheSignedInAdmin() throws Exception {
        when(lmsApprovedAccountService.create(any(LmsApprovedAccountDto.class), eq("admin"))).thenReturn(iu());

        mockMvc.perform(post("/config/lms-approved-accounts").principal(ADMIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.statusUpdatedBy").value("admin"));
    }

    @Test
    public void testExplainsABadRequest() throws Exception {
        when(lmsApprovedAccountService.create(any(LmsApprovedAccountDto.class), eq("admin")))
            .thenThrow(new IllegalArgumentException("[iu.instructure.com] already belongs to the account [IU]."));

        mockMvc.perform(post("/config/lms-approved-accounts").principal(ADMIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("[iu.instructure.com] already belongs to the account [IU]."));
    }

    @Test
    public void testUpdatesAnAccount() throws Exception {
        LmsApprovedAccountDto iu = iu();
        when(lmsApprovedAccountService.update(eq(iu.getUuid()), any(LmsApprovedAccountDto.class), eq("admin"))).thenReturn(Optional.of(iu));

        mockMvc.perform(put("/config/lms-approved-accounts/" + iu.getUuid()).principal(ADMIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uuid").value(iu.getUuid().toString()));
    }

    @Test
    public void testUpdateOfAMissingAccountIsNotFound() throws Exception {
        when(lmsApprovedAccountService.update(any(UUID.class), any(LmsApprovedAccountDto.class), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/config/lms-approved-accounts/" + UUID.randomUUID()).principal(ADMIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("No LMS account with that ID."));
    }

    @Test
    public void testUpdateExplainsABadRequest() throws Exception {
        when(lmsApprovedAccountService.update(any(UUID.class), any(LmsApprovedAccountDto.class), any())).thenThrow(new IllegalArgumentException("An account needs a name."));

        mockMvc.perform(put("/config/lms-approved-accounts/" + UUID.randomUUID()).principal(ADMIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("An account needs a name."));
    }

}
