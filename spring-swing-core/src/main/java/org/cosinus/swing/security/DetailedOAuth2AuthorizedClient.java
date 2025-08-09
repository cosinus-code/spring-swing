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

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.util.Map;

public class DetailedOAuth2AuthorizedClient extends OAuth2AuthorizedClient {

    private final Map<String, Object> details;

    public DetailedOAuth2AuthorizedClient(ClientRegistration clientRegistration,
                                          String principalName,
                                          OAuth2AccessToken accessToken,
                                          @Nullable OAuth2RefreshToken refreshToken,
                                          final Map<String, Object> details) {
        super(clientRegistration, principalName, accessToken, refreshToken);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
