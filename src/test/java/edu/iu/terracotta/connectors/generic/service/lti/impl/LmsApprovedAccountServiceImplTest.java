package edu.iu.terracotta.connectors.generic.service.lti.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccountHost;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.LmsApprovedAccountDto;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.LmsApprovedAccountHostRepository;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.LmsApprovedAccountRepository;

public class LmsApprovedAccountServiceImplTest {

    private LmsApprovedAccountRepository lmsApprovedAccountRepository;
    private LmsApprovedAccountHostRepository lmsApprovedAccountHostRepository;
    private LmsApprovedAccountServiceImpl service;

    @BeforeEach
    public void beforeEach() {
        lmsApprovedAccountRepository = mock(LmsApprovedAccountRepository.class);
        lmsApprovedAccountHostRepository = mock(LmsApprovedAccountHostRepository.class);
        service = new LmsApprovedAccountServiceImpl(lmsApprovedAccountRepository, lmsApprovedAccountHostRepository);

        when(lmsApprovedAccountRepository.save(any(LmsApprovedAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lmsApprovedAccountHostRepository.findByHost(anyString())).thenReturn(Optional.empty());
    }

    private static LmsApprovedAccount account(long id, String name, boolean approved, String... hosts) {
        LmsApprovedAccount account = LmsApprovedAccount.builder().name(name).lmsConnector(LmsConnector.CANVAS).approved(approved).build();
        account.setId(id);
        account.setUuid(UUID.randomUUID());

        for (String host : hosts) {
            account.getHosts().add(LmsApprovedAccountHost.builder().host(host).lmsApprovedAccount(account).build());
        }

        return account;
    }

    @Test
    public void testNormalizeHostTakesAHostOrAUrl() {
        assertEquals("iu.instructure.com", service.normalizeHost(" IU.instructure.com "));
        assertEquals("canvas.iu.edu", service.normalizeHost("https://Canvas.IU.edu/courses/1"));
        assertEquals("canvas.iu.edu", service.normalizeHost("canvas.iu.edu:8443"));
        assertNull(service.normalizeHost(" "));
        assertNull(service.normalizeHost(null));
        assertNull(service.normalizeHost("https://"));
    }

    @Test
    public void testFindsAnAccountByAnyOfItsHosts() {
        LmsApprovedAccount iu = account(1, "Indiana University", true, "iu.instructure.com", "canvas.iu.edu");
        when(lmsApprovedAccountHostRepository.findByHostAndLmsApprovedAccount_LmsConnector("canvas.iu.edu", LmsConnector.CANVAS))
            .thenReturn(Optional.of(iu.getHosts().get(1)));

        assertSame(iu, service.findByHost(LmsConnector.CANVAS, "Canvas.IU.edu").orElseThrow());
        assertTrue(service.findByHost(LmsConnector.CANVAS, null).isEmpty());
    }

    @Test
    public void testFindOrCreateAddsAnUnapprovedAccountForANewHost() {
        when(lmsApprovedAccountHostRepository.findByHostAndLmsApprovedAccount_LmsConnector("new.instructure.com", LmsConnector.CANVAS)).thenReturn(Optional.empty());

        LmsApprovedAccount created = service.findOrCreate(LmsConnector.CANVAS, "New.instructure.com");

        assertFalse(created.isApproved());
        assertEquals("new.instructure.com", created.getName());
        assertEquals(LmsConnector.CANVAS, created.getLmsConnector());
        assertEquals("new.instructure.com", created.getHosts().get(0).getHost());
        assertSame(created, created.getHosts().get(0).getLmsApprovedAccount());
    }

    @Test
    public void testFindOrCreateReusesAnExistingAccount() {
        LmsApprovedAccount iu = account(1, "Indiana University", false, "iu.instructure.com");
        when(lmsApprovedAccountHostRepository.findByHostAndLmsApprovedAccount_LmsConnector("iu.instructure.com", LmsConnector.CANVAS)).thenReturn(Optional.of(iu.getHosts().get(0)));

        assertSame(iu, service.findOrCreate(LmsConnector.CANVAS, "iu.instructure.com"));
        verify(lmsApprovedAccountRepository, never()).save(any());
    }

    @Test
    public void testADeploymentSetUpByHandAlwaysLaunches() {
        assertTrue(service.allowsLaunch(new PlatformDeployment(), Map.of("guid", "abc")));
        assertTrue(service.allowsLaunch(null, null));
        verify(lmsApprovedAccountRepository, never()).save(any());
    }

    @Test
    public void testOnlyAnApprovedAccountsDeploymentLaunches() {
        PlatformDeployment approved = new PlatformDeployment();
        approved.setLmsApprovedAccount(account(1, "Approved", true));
        PlatformDeployment pending = new PlatformDeployment();
        pending.setLmsApprovedAccount(account(2, "Pending", false));

        assertTrue(service.allowsLaunch(approved, null));
        assertFalse(service.allowsLaunch(pending, null));
    }

    @Test
    public void testRecordsThePlatformGuidOnce() {
        LmsApprovedAccount account = account(1, "Indiana University", false);
        PlatformDeployment platformDeployment = new PlatformDeployment();
        platformDeployment.setLmsApprovedAccount(account);

        service.allowsLaunch(platformDeployment, Map.of("guid", "root-account-guid:canvas-lms"));
        service.allowsLaunch(platformDeployment, Map.of("guid", "something-else"));

        assertEquals("root-account-guid:canvas-lms", account.getPlatformGuid());
        verify(lmsApprovedAccountRepository).save(account);
    }

    @Test
    public void testCreateNormalizesHostsAndRecordsWhoApproved() {
        LmsApprovedAccountDto created = service.create(
            LmsApprovedAccountDto.builder().name(" Indiana University ").lmsConnector(LmsConnector.CANVAS).approved(true).hosts(List.of("https://IU.instructure.com/", "canvas.iu.edu", "iu.instructure.com")).build(),
            "admin"
        );

        assertEquals("Indiana University", created.getName());
        assertEquals(List.of("iu.instructure.com", "canvas.iu.edu"), created.getHosts());
        assertTrue(created.isApproved());
        assertEquals("admin", created.getApprovedBy());
        assertNotNull(created.getApprovedAt());
    }

    @Test
    public void testCreateNeedsANameAndAnLms() {
        assertThrows(IllegalArgumentException.class, () -> service.create(LmsApprovedAccountDto.builder().lmsConnector(LmsConnector.CANVAS).build(), "admin"));
        assertThrows(IllegalArgumentException.class, () -> service.create(LmsApprovedAccountDto.builder().name("IU").build(), "admin"));
        assertThrows(IllegalArgumentException.class, () -> service.create(null, "admin"));
    }

    @Test
    public void testAHostBelongsToOneAccount() {
        LmsApprovedAccount iu = account(1, "Indiana University", true, "iu.instructure.com");
        when(lmsApprovedAccountHostRepository.findByHost("iu.instructure.com")).thenReturn(Optional.of(iu.getHosts().get(0)));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.create(LmsApprovedAccountDto.builder().name("Other").lmsConnector(LmsConnector.CANVAS).hosts(List.of("iu.instructure.com")).build(), "admin")
        );

        assertTrue(exception.getMessage().contains("Indiana University"), exception.getMessage());
    }

    @Test
    public void testCreateRefusesSomethingThatIsntAHost() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.create(LmsApprovedAccountDto.builder().name("IU").lmsConnector(LmsConnector.CANVAS).hosts(List.of(" ")).build(), "admin")
        );
    }

    @Test
    public void testUpdateApprovesThenRevokes() {
        LmsApprovedAccount pending = account(1, "new.instructure.com", false, "new.instructure.com");
        when(lmsApprovedAccountRepository.findByUuid(pending.getUuid())).thenReturn(Optional.of(pending));

        LmsApprovedAccountDto approved = service.update(pending.getUuid(), LmsApprovedAccountDto.builder().name("New University").approved(true).build(), "admin").orElseThrow();

        assertTrue(approved.isApproved());
        assertEquals("New University", approved.getName());
        assertEquals("admin", approved.getApprovedBy());
        // no hosts in the update: the account keeps its own
        assertEquals(List.of("new.instructure.com"), approved.getHosts());

        LmsApprovedAccountDto revoked = service.update(pending.getUuid(), LmsApprovedAccountDto.builder().name("New University").approved(false).build(), "admin").orElseThrow();

        assertFalse(revoked.isApproved());
        assertNull(revoked.getApprovedBy());
        assertNull(revoked.getApprovedAt());
    }

    @Test
    public void testUpdateReplacesHostsKeepingTheOnesThatStay() {
        LmsApprovedAccount iu = account(1, "Indiana University", true, "iu.instructure.com", "old.iu.edu");
        LmsApprovedAccountHost kept = iu.getHosts().get(0);
        when(lmsApprovedAccountRepository.findByUuid(iu.getUuid())).thenReturn(Optional.of(iu));
        // its own host isn't a conflict
        when(lmsApprovedAccountHostRepository.findByHost("iu.instructure.com")).thenReturn(Optional.of(kept));

        LmsApprovedAccountDto updated = service.update(iu.getUuid(), LmsApprovedAccountDto.builder().name("Indiana University").approved(true).hosts(List.of("iu.instructure.com", "canvas.iu.edu")).build(), "admin").orElseThrow();

        assertEquals(List.of("iu.instructure.com", "canvas.iu.edu"), updated.getHosts());
        assertSame(kept, iu.getHosts().get(0));
    }

    @Test
    public void testUpdateOfAMissingAccount() {
        assertTrue(service.update(UUID.randomUUID(), LmsApprovedAccountDto.builder().name("x").build(), "admin").isEmpty());
    }

    @Test
    public void testGetAllListsByName() {
        when(lmsApprovedAccountRepository.findAllByOrderByNameAsc()).thenReturn(List.of(account(1, "A", true, "a.edu"), account(2, "B", false)));

        List<LmsApprovedAccountDto> all = service.getAll();

        assertEquals(List.of("A", "B"), all.stream().map(LmsApprovedAccountDto::getName).toList());
        assertEquals(List.of("a.edu"), all.get(0).getHosts());
    }

}
