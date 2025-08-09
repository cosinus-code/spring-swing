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

package org.cosinus.swing.security.receiver;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;

import java.io.IOException;

public interface OAuth2AuthenticationReceiver extends AutoCloseable {

    String LOCALHOST = "localhost";

    int DEFAULT_PORT = 8888;

    String CALLBACK_PATH = "/Callback";

    String REDIRECT_URI_PATTERN = "http://%s:%d%s";

    /**
     * Start the authentication receiver.
     *
     * @throws IOException for any input/output error
     */
    void start() throws IOException;

    /**
     * Execute an authorization request.
     *
     * @param authorizationRequest the resuest to execute
     * @return the authorization response (success or error)
     * @throws IOException for any input/output error
     */
    OAuth2AuthorizationResponse execute(final OAuth2AuthorizationRequest authorizationRequest) throws IOException;

    /**
     * Releases any resources and stops any processes started.
     *
     * @throws IOException for any input/output error
     */
    void stop() throws IOException;

    /**
     * Close the server.
     *
     * @throws IOException for any input/output error
     */
    @Override
    default void close() throws IOException {
        stop();
    }
}
