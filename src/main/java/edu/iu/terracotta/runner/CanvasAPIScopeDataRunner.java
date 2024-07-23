package edu.iu.terracotta.runner;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import edu.iu.terracotta.model.canvas.CanvasAPIScope;
import edu.iu.terracotta.model.canvas.CanvasAPITokenEntity;
import edu.iu.terracotta.model.canvas.CanvasAPITokenScope;
import edu.iu.terracotta.repository.CanvasAPITokenRepository;
import edu.iu.terracotta.service.canvas.CanvasAPIScopeService;
import edu.iu.terracotta.service.canvas.CanvasAPITokenScopeService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class CanvasAPIScopeDataRunner implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired private CanvasAPIScopeService canvasAPIScopeService;
    @Autowired private CanvasAPITokenScopeService canvasAPITokenScopeService;
    @Autowired private CanvasAPITokenRepository canvasAPITokenRepository;

    @Value("${app.canvas.api.scope.update.enabled:false}")
    private boolean enabled;

    @Value("${app.canvas.api.scope.update.batchsize:100}")
    private int batchSize;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!enabled) {
            return;
        }

        Thread thread = new Thread(
            () ->
                {
                    List<CanvasAPIScope> canvasAPIRequiredScopes = canvasAPIScopeService.getScopesByRequired(true);
                    List<CanvasAPITokenScope> canvasAPITokenScopes = canvasAPITokenScopeService.getAllTokenScopes();
                    List<Long> canvasAPITokenIdsWithScopes = canvasAPITokenScopes.stream()
                        .map(c -> c.getCanvasAPITokenEntity().getTokenId())
                        .toList();

                    // update Canvas API token scopes
                    int page = 0;
                    Page<CanvasAPITokenEntity> canvasAPITokenEntities = canvasAPITokenRepository.findAll(PageRequest.of(page++, batchSize));

                    log.info("Starting Canvas API token scope updates...");
                    AtomicInteger processed = new AtomicInteger(0);

                    while (CollectionUtils.isNotEmpty(canvasAPITokenEntities.getContent())) {
                        canvasAPITokenEntities.getContent().stream()
                            .filter(
                                canvasAPITokenEntity -> canvasAPITokenIdsWithScopes.stream()
                                    .filter(c -> c.equals(canvasAPITokenEntity.getTokenId()))
                                    .count() < canvasAPIRequiredScopes.size()
                            )
                            .forEach(
                               canvasAPITokenEntity-> {
                                    // delete any existing token scopes
                                    canvasAPITokenScopeService.deleteScopesForTokenId(canvasAPITokenEntity.getTokenId());

                                    List<CanvasAPITokenScope> canvasAPITokenScopesToCreate = canvasAPIRequiredScopes.stream()
                                        .map(
                                            canvasAPIRequiredScope -> {
                                                CanvasAPITokenScope canvasAPITokenScope = new CanvasAPITokenScope();
                                                canvasAPITokenScope.setCanvasAPIScope(canvasAPIRequiredScope);
                                                canvasAPITokenScope.setCanvasAPITokenEntity(canvasAPITokenEntity);
                                                return canvasAPITokenScope;
                                            }
                                        )
                                        .toList();

                                    canvasAPITokenScopeService.createTokenScopes(canvasAPITokenScopesToCreate);
                                    processed.incrementAndGet();
                                }
                            );

                        log.info("Processed {} Canvas API Token records...", processed.get());
                        canvasAPITokenEntities = canvasAPITokenRepository.findAll(PageRequest.of(page++, batchSize));
                    }

                    log.info("Canvas API token update complete! {} Canvas API token records processed.", processed.get());
                }
        );

        thread.start();
    }

}
