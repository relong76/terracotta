package edu.iu.terracotta.connectors.generic.service.lti.impl;

import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private static final String PLATFORM_GUID = "guid";

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
    @Transactional
    public LmsApprovedAccount findOrCreate(LmsConnector lmsConnector, String host) {
        String normalized = normalizeHost(host);

        return findByHost(lmsConnector, normalized).orElseGet(() -> {
            LmsApprovedAccount account = LmsApprovedAccount.builder()
                .name(normalized)
                .lmsConnector(lmsConnector)
                .approved(false)
                .build();
            account.getHosts().add(LmsApprovedAccountHost.builder().host(normalized).lmsApprovedAccount(account).build());
            log.info("Added an unapproved {} account for [{}]: its launches wait for approval", lmsConnector, normalized);

            return lmsApprovedAccountRepository.save(account);
        });
    }

    @Override
    @Transactional
    public boolean allowsLaunch(PlatformDeployment platformDeployment, Map<String, Object> toolPlatform) {
        LmsApprovedAccount account = platformDeployment == null ? null : platformDeployment.getLmsApprovedAccount();

        if (account == null) {
            return true;
        }

        if (StringUtils.isBlank(account.getPlatformGuid()) && toolPlatform != null && toolPlatform.get(PLATFORM_GUID) instanceof String guid && StringUtils.isNotBlank(guid)) {
            account.setPlatformGuid(guid);
            lmsApprovedAccountRepository.save(account);
        }

        if (!account.isApproved()) {
            log.warn("Refused a launch through platform deployment ID: [{}]: its {} account [{}] isn't approved", platformDeployment.getKeyId(), account.getLmsConnector(), account.getName());
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
        setApproved(account, dto.isApproved(), adminName);

        return toDto(lmsApprovedAccountRepository.save(account));
    }

    @Override
    @Transactional
    public Optional<LmsApprovedAccountDto> update(UUID uuid, LmsApprovedAccountDto dto, String adminName) {
        Optional<LmsApprovedAccount> found = lmsApprovedAccountRepository.findByUuid(uuid);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        if (dto == null || StringUtils.isBlank(dto.getName())) {
            throw new IllegalArgumentException("An account needs a name.");
        }

        LmsApprovedAccount account = found.get();
        account.setName(dto.getName().strip());

        if (dto.getHosts() != null) {
            setHosts(account, dto.getHosts());
        }

        setApproved(account, dto.isApproved(), adminName);

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

    private static void setApproved(LmsApprovedAccount account, boolean approved, String adminName) {
        if (approved && !account.isApproved()) {
            account.setApprovedBy(adminName);
            account.setApprovedAt(new Timestamp(System.currentTimeMillis()));
            log.info("{} approved the {} account [{}]", adminName, account.getLmsConnector(), account.getName());
        } else if (!approved && account.isApproved()) {
            account.setApprovedBy(null);
            account.setApprovedAt(null);
            log.info("{} revoked approval of the {} account [{}]", adminName, account.getLmsConnector(), account.getName());
        }

        account.setApproved(approved);
    }

    private static LmsApprovedAccountDto toDto(LmsApprovedAccount account) {
        List<String> hosts = new ArrayList<>(account.getHosts().stream().map(LmsApprovedAccountHost::getHost).toList());

        return LmsApprovedAccountDto.builder()
            .uuid(account.getUuid())
            .name(account.getName())
            .lmsConnector(account.getLmsConnector())
            .approved(account.isApproved())
            .platformGuid(account.getPlatformGuid())
            .approvedBy(account.getApprovedBy())
            .approvedAt(account.getApprovedAt())
            .hosts(hosts)
            .build();
    }

}
