/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.util.StringUtils;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
import static org.springframework.util.Assert.notNull;

public class OAuth2AccessTokenInterceptor implements RequestInterceptor {

    public static final String ANONYMOUS_USER = "anonymousUser";

    public static final String ANONYMOUS_ROLE = "ROLE_ANONYMOUS";

    public static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken(
        ANONYMOUS_USER, ANONYMOUS_USER, createAuthorityList(ANONYMOUS_ROLE));

    private final String tokenType;

    private final String header;

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public OAuth2AccessTokenInterceptor(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        this(BEARER.getValue(), AUTHORIZATION, oAuth2AuthorizedClientManager);
    }

    public OAuth2AccessTokenInterceptor(final String tokenType,
                                        final String header,
                                        final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        this.tokenType = tokenType;
        this.header = header;
        this.authorizedClientManager = oAuth2AuthorizedClientManager;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(header, "%s %s".formatted(tokenType, getToken(template).getTokenValue()));
    }

    public OAuth2AccessToken getToken(RequestTemplate template) {
        return ofNullable(getClientRegistrationId(template))
            .map(this::getToken)
            .orElseThrow(() -> new IllegalStateException("OAuth2 token has not been successfully acquired."));
    }

    protected OAuth2AccessToken getToken(String clientRegistrationId) {
        final Authentication authentication = ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(principal -> !isEmpty(principal.getName()))
            .orElse(ANONYMOUS_AUTHENTICATION);

        return ofNullable(clientRegistrationId)
            .filter(StringUtils::hasText)
            .map(OAuth2AuthorizeRequest::withClientRegistrationId)
            .map(builder -> builder.principal(authentication))
            .map(OAuth2AuthorizeRequest.Builder::build)
            .map(authorizedClientManager::authorize)
            .map(OAuth2AuthorizedClient::getAccessToken)
            .orElse(null);
    }

    protected String getClientRegistrationId(RequestTemplate template) {
        Target<?> feignTarget = template.feignTarget();
        notNull(feignTarget, "FeignTarget may not be null.");
        return feignTarget.name();
    }
}
