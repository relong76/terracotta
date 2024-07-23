package edu.iu.terracotta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.canvas.CanvasAPITokenScope;
import jakarta.transaction.Transactional;

@SuppressWarnings("PMD.MethodNamingConventions")
public interface CanvasAPITokenScopeRepository extends JpaRepository<CanvasAPITokenScope, Long> {

    List<CanvasAPITokenScope> findAllByCanvasAPITokenEntity_TokenId(long tokenId);
    List<CanvasAPITokenScope> findAllByCanvasAPIScope_Id(long scopeId);

    @Transactional
    void deleteByCanvasAPITokenEntity_TokenId(long tokenId);

    @Transactional
    void deleteByCanvasAPIScope_Id(long scopeId);

}
