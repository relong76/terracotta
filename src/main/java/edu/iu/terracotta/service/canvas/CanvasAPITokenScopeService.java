package edu.iu.terracotta.service.canvas;

import java.util.List;

import edu.iu.terracotta.model.canvas.CanvasAPITokenScope;

public interface CanvasAPITokenScopeService {

    List<CanvasAPITokenScope> getAllTokenScopes();
    List<CanvasAPITokenScope> getScopesForTokenId(long tokenId);
    List<CanvasAPITokenScope> getTokensForScopeId(long scopeId);
    CanvasAPITokenScope createTokenScope(CanvasAPITokenScope canvasAPITokenScope);
    List<CanvasAPITokenScope> createTokenScopes(List<CanvasAPITokenScope> canvasAPITokenScopes);
    CanvasAPITokenScope updateTokenScope(CanvasAPITokenScope canvasAPITokenScope);
    List<CanvasAPITokenScope> updateTokenScopes(List<CanvasAPITokenScope> canvasAPITokenScopes);
    void deleteTokenScope(long id);
    void deleteScopesForTokenId(long tokenId);

}
