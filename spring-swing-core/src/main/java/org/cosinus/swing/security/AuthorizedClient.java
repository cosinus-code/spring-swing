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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.time.Instant.ofEpochMilli;
import static java.util.Optional.ofNullable;

@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedClient implements Serializable {

    private String registrationId;

    private String principalName;

    private String accessTokenType;

    private String accessTokenValue;

    private long accessTokenIssuedIn;

    private long accessTokenExpiresIn;

    private String refreshTokenValue;

    private long refreshTokenIssuedIn;

    private long refreshTokenExpiresIn;

    private Map<String, Object> details;

    public AuthorizedClient() {

    }

    public AuthorizedClient(OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        registrationId = ofNullable(oAuth2AuthorizedClient.getClientRegistration())
            .map(ClientRegistration::getRegistrationId)
            .orElse(null);
        principalName = oAuth2AuthorizedClient.getPrincipalName();
        ofNullable(oAuth2AuthorizedClient.getAccessToken())
            .ifPresent(accessToken -> {
                ofNullable(accessToken.getTokenType())
                    .map(OAuth2AccessToken.TokenType::getValue)
                    .ifPresent(this::setAccessTokenType);
                accessTokenValue = accessToken.getTokenValue();
                ofNullable(accessToken.getIssuedAt())
                    .map(Instant::toEpochMilli)
                    .ifPresent(this::setAccessTokenIssuedIn);
                ofNullable(accessToken.getExpiresAt())
                    .map(Instant::toEpochMilli)
                    .ifPresent(this::setAccessTokenExpiresIn);
            });
        ofNullable(oAuth2AuthorizedClient.getRefreshToken())
            .ifPresent(refreshToken -> {
                refreshTokenValue = refreshToken.getTokenValue();
                ofNullable(refreshToken.getIssuedAt())
                    .map(Instant::toEpochMilli)
                    .ifPresent(this::setRefreshTokenIssuedIn);
                ofNullable(refreshToken.getExpiresAt())
                    .map(Instant::toEpochMilli)
                    .ifPresent(this::setRefreshTokenExpiresIn);
            });
        accessTokenType = oAuth2AuthorizedClient.getAccessToken().getTokenType().getValue();

        if (oAuth2AuthorizedClient instanceof DetailedOAuth2AuthorizedClient detailedOAuth2AuthorizedClient) {
            details = detailedOAuth2AuthorizedClient.getDetails();
        }
    }

    public DetailedOAuth2AuthorizedClient toOAuth2AuthorizedClient(final ClientRegistration clientRegistration) {
        return new DetailedOAuth2AuthorizedClient(
            clientRegistration,
            getPrincipalName(),
            new OAuth2AccessToken(
                new OAuth2AccessToken.TokenType(getAccessTokenType()),
                getAccessTokenValue(),
                ofEpochMilli(getAccessTokenIssuedIn()),
                ofEpochMilli(getAccessTokenExpiresIn())),
            new OAuth2RefreshToken(getRefreshTokenValue(),
                ofEpochMilli(getAccessTokenIssuedIn()),
                ofEpochMilli(getAccessTokenExpiresIn())),
            getDetails());
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAccessTokenType() {
        return accessTokenType;
    }

    public void setAccessTokenType(String accessTokenType) {
        this.accessTokenType = accessTokenType;
    }

    public String getAccessTokenValue() {
        return accessTokenValue;
    }

    public void setAccessTokenValue(String accessTokenValue) {
        this.accessTokenValue = accessTokenValue;
    }

    public long getAccessTokenIssuedIn() {
        return accessTokenIssuedIn;
    }

    public void setAccessTokenIssuedIn(long accessTokenIssuedIn) {
        this.accessTokenIssuedIn = accessTokenIssuedIn;
    }

    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    public void setAccessTokenExpiresIn(long accessTokenExpiresIn) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public String getRefreshTokenValue() {
        return refreshTokenValue;
    }

    public void setRefreshTokenValue(String refreshToken) {
        this.refreshTokenValue = refreshToken;
    }

    public long getRefreshTokenIssuedIn() {
        return refreshTokenIssuedIn;
    }

    public void setRefreshTokenIssuedIn(long refreshTokenIssuedIn) {
        this.refreshTokenIssuedIn = refreshTokenIssuedIn;
    }

    public long getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    public void setRefreshTokenExpiresIn(long refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
