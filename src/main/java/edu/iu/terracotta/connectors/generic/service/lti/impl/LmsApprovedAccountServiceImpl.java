package edu.iu.terracotta.connectors.generic.service.lti.impl;

import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccountHost;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsApprovedAccountStatus;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.LmsApprovedAccountDto;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.LmsApprovedAccountHostRepository;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.LmsApprovedAccountRepository;
import edu.iu.terracotta.connectors.generic.service.lti.LmsApprovedAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class LmsApprovedAccountServiceImpl implements LmsApprovedAccountService {

    private final LmsApprovedAccountRepository lmsApprovedAccountRepository;
    private final LmsApprovedAccountHostRepository lmsApprovedAccountHostRepository;

    @Override
    public Optional<LmsApprovedAccount> findByHost(LmsConnector lmsConnector, String host) {
        String normalized = normalizeHost(host);

        if (normalized == null) {
            return Optional.empty();
        }

        return lmsApprovedAccountHostRepository.findByHostAndLmsApprovedAccount_LmsConnector(normalized, lmsConnector)
            .map(LmsApprovedAccountHost::getLmsApprovedAccount);
    }

    @Override
    public Optional<LmsApprovedAccount> find(LmsConnector lmsConnector, String host, String platformGuid) {
        if (StringUtils.isNotBlank(platformGuid)) {
            Optional<LmsApprovedAccount> byGuid = lmsApprovedAccountRepository.findByLmsConnectorAndPlatformGuid(lmsConnector, platformGuid);

            if (byGuid.isPresent()) {
                return byGuid;
            }
        }

        Optional<LmsApprovedAccount> byHost = findByHost(lmsConnector, host);

        if (byHost.isPresent() && StringUtils.isNoneBlank(platformGuid, byHost.get().getPlatformGuid()) && !platformGuid.equals(byHost.get().getPlatformGuid())) {
            throw new IllegalStateException(String.format("[%s] belongs to the account [%s], which has another LMS account ID", normalizeHost(host), byHost.get().getName()));
        }

        return byHost;
    }

    @Override
    @Transactional
    public LmsApprovedAccount findOrCreate(LmsConnector lmsConnector, String host, String platformGuid, String name) {
        String normalized = normalizeHost(host);
        String guid = StringUtils.trimToNull(platformGuid);
        Optional<LmsApprovedAccount> found = find(lmsConnector, normalized, guid);

        if (found.isPresent()) {
            LmsApprovedAccount account = found.get();

            // e.g. an account added by address, by hand or by the migration, registering for the first time
            if (guid != null && StringUtils.isBlank(account.getPlatformGuid())) {
                account.setPlatformGuid(guid);

                return lmsApprovedAccountRepository.save(account);
            }

            return account;
        }

        LmsApprovedAccount account = LmsApprovedAccount.builder()
            .name(StringUtils.defaultIfBlank(StringUtils.trimToNull(name), normalized))
            .lmsConnector(lmsConnector)
            .platformGuid(guid)
            .status(LmsApprovedAccountStatus.PENDING)
            .build();
        account.getHosts().add(LmsApprovedAccountHost.builder().host(normalized).lmsApprovedAccount(account).build());
        log.info("Added a pending {} account [{}] for [{}]: its launches wait for approval", lmsConnector, account.getName(), normalized);

        return lmsApprovedAccountRepository.save(account);
    }

    @Override
    public boolean allowsLaunch(PlatformDeployment platformDeployment) {
        LmsApprovedAccount account = platformDeployment == null ? null : platformDeployment.getLmsApprovedAccount();

        if (account == null) {
            return true;
        }

        if (!account.isApproved()) {
            log.warn("Refused a launch through platform deployment ID: [{}]: its {} account [{}] is {}", platformDeployment.getKeyId(), account.getLmsConnector(), account.getName(), account.getStatus());
        }

        return account.isApproved();
    }

    @Override
    public List<LmsApprovedAccountDto> getAll() {
        return lmsApprovedAccountRepository.findAllByOrderByNameAsc().stream()
            .map(LmsApprovedAccountServiceImpl::toDto)
            .toList();
    }

    @Override
    @Transactional
    public LmsApprovedAccountDto create(LmsApprovedAccountDto dto, String adminName) {
        if (dto == null || StringUtils.isBlank(dto.getName()) || dto.getLmsConnector() == null) {
            throw new IllegalArgumentException("An account needs a name and an lmsConnector.");
        }

        LmsApprovedAccount account = LmsApprovedAccount.builder()
            .name(dto.getName().strip())
            .lmsConnector(dto.getLmsConnector())
            .build();
        setHosts(account, dto.getHosts());
        setStatus(account, dto.getStatus() != null ? dto.getStatus() : LmsApprovedAccountStatus.PENDING, adminName);

        return toDto(lmsApprovedAccountRepository.save(account));
    }

    @Override
    @Transactional
    public Optional<LmsApprovedAccountDto> update(UUID uuid, LmsApprovedAccountDto dto, String adminName) {
        Optional<LmsApprovedAccount> found = lmsApprovedAccountRepository.findByUuid(uuid);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        if (dto == null || StringUtils.isBlank(dto.getName()) || dto.getStatus() == null) {
            throw new IllegalArgumentException("An account needs a name and a status.");
        }

        LmsApprovedAccount account = found.get();
        account.setName(dto.getName().strip());

        if (dto.getHosts() != null) {
            setHosts(account, dto.getHosts());
        }

        setStatus(account, dto.getStatus(), adminName);

        return Optional.of(toDto(lmsApprovedAccountRepository.save(account)));
    }

    @Override
    public String normalizeHost(String hostOrUrl) {
        if (StringUtils.isBlank(hostOrUrl)) {
            return null;
        }

        String value = hostOrUrl.strip();

        try {
            String host = Strings.CS.contains(value, "://") ? URI.create(value).getHost() : URI.create("https://" + value).getHost();

            return StringUtils.isBlank(host) ? null : host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    private void setHosts(LmsApprovedAccount account, List<String> hosts) {
        Set<String> normalized = new LinkedHashSet<>();

        for (String host : CollectionUtils.emptyIfNull(hosts)) {
            String value = normalizeHost(host);

            if (value == null) {
                throw new IllegalArgumentException(String.format("[%s] isn't a host name.", host));
            }

            lmsApprovedAccountHostRepository.findByHost(value)
                .filter(existing -> existing.getLmsApprovedAccount() != account && !existing.getLmsApprovedAccount().getId().equals(account.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(String.format("[%s] already belongs to the account [%s].", value, existing.getLmsApprovedAccount().getName()));
                });

            normalized.add(value);
        }

        // keep the rows for hosts that stay, so their IDs don't change
        account.getHosts().removeIf(host -> !normalized.contains(host.getHost()));
        normalized.stream()
            .filter(value -> account.getHosts().stream().noneMatch(host -> host.getHost().equals(value)))
            .forEach(value -> account.getHosts().add(LmsApprovedAccountHost.builder().host(value).lmsApprovedAccount(account).build()));
    }

    // records who changed it, only when it changes
    private static void setStatus(LmsApprovedAccount account, LmsApprovedAccountStatus status, String adminName) {
        if (account.getId() != null && account.getStatus() == status) {
            return;
        }

        log.info("{} set the {} account [{}] to {} (was {})", adminName, account.getLmsConnector(), account.getName(), status, account.getId() != null ? account.getStatus() : "new");
        account.setStatus(status);
        account.setStatusUpdatedBy(adminName);
        account.setStatusUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }

    private static LmsApprovedAccountDto toDto(LmsApprovedAccount account) {
        List<String> hosts = new ArrayList<>(account.getHosts().stream().map(LmsApprovedAccountHost::getHost).toList());

        return LmsApprovedAccountDto.builder()
            .uuid(account.getUuid())
            .name(account.getName())
            .lmsConnector(account.getLmsConnector())
            .status(account.getStatus())
            .platformGuid(account.getPlatformGuid())
            .statusUpdatedBy(account.getStatusUpdatedBy())
            .statusUpdatedAt(account.getStatusUpdatedAt())
            .hosts(hosts)
            .build();
    }

}
