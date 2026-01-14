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

package org.cosinus.swing.cloud.client;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.function.Function;

import static org.cosinus.swing.security.OAuth2AccessTokenInterceptor.ANONYMOUS_ROLE;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

public class ClientInvoker<C> {

    protected final C client;

    public ClientInvoker(final C client) {
        this.client = client;
    }

    public <T> T invoke(final String userId, final Function<C, T> clientCall) {
        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken(userId, userId, getUserAuthorities(userId)));
        try {
            return clientCall.apply(client);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    public Collection<? extends GrantedAuthority> getUserAuthorities(String userId) {
        return createAuthorityList(ANONYMOUS_ROLE);
    }
}
