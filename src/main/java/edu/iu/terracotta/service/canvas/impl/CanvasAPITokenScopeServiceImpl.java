package edu.iu.terracotta.service.canvas.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.model.canvas.CanvasAPITokenScope;
import edu.iu.terracotta.repository.CanvasAPITokenScopeRepository;
import edu.iu.terracotta.service.canvas.CanvasAPITokenScopeService;

@Service
public class CanvasAPITokenScopeServiceImpl implements CanvasAPITokenScopeService {

    @Autowired private CanvasAPITokenScopeRepository canvasAPITokenScopeRepository;

    @Override
    public List<CanvasAPITokenScope> getAllTokenScopes() {
        return canvasAPITokenScopeRepository.findAll();
    }

    @Override
    public List<CanvasAPITokenScope> getScopesForTokenId(long tokenId) {
        return canvasAPITokenScopeRepository.findAllByCanvasAPITokenEntity_TokenId(tokenId);
    }

    @Override
    public List<CanvasAPITokenScope> getTokensForScopeId(long scopeId) {
        return canvasAPITokenScopeRepository.findAllByCanvasAPIScope_Id(scopeId);
    }

    @Override
    public CanvasAPITokenScope createTokenScope(CanvasAPITokenScope canvasAPITokenScope) {
        return canvasAPITokenScopeRepository.save(canvasAPITokenScope);
    }

    @Override
    public List<CanvasAPITokenScope> createTokenScopes(List<CanvasAPITokenScope> canvasAPITokenScopes) {
        return canvasAPITokenScopeRepository.saveAll(canvasAPITokenScopes);
    }

    @Override
    public CanvasAPITokenScope updateTokenScope(CanvasAPITokenScope canvasAPITokenScope) {
        return canvasAPITokenScopeRepository.save(canvasAPITokenScope);
    }

    @Override
    public List<CanvasAPITokenScope> updateTokenScopes(List<CanvasAPITokenScope> canvasAPITokenScopes) {
        return canvasAPITokenScopeRepository.saveAll(canvasAPITokenScopes);
    }

    @Override
    public void deleteTokenScope(long id) {
        canvasAPITokenScopeRepository.deleteById(id);
    }

    @Override
    public void deleteScopesForTokenId(long tokenId) {
        canvasAPITokenScopeRepository.deleteByCanvasAPITokenEntity_TokenId(tokenId);
    }

}
