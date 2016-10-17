package com.treem.treem.services.oauth;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * Custom o auth request to add body for delete requests
 */
public class AdvancedOAuthRequest extends OAuthRequest {
    private Verb verb;

    /**
     * Create advanced oauth request
     * @param verb request type
     * @param url request url
     * @param service o auth service
     */
    public AdvancedOAuthRequest(Verb verb, String url, OAuthService service) {
        super(verb, url, service);
        this.verb = verb;
    }

    /**
     * Return parent has body request result and add true for delete requests
     * @return true if request may have body
     */
    @Override
    protected boolean hasBodyContent() {
        return super.hasBodyContent()||verb==Verb.DELETE;
    }
}
