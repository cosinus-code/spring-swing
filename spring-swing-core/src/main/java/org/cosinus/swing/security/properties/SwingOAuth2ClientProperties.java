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

package org.cosinus.swing.security.properties;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.security.oauth2.client")
public class SwingOAuth2ClientProperties extends OAuth2ClientProperties {

    private OAuth2ClientReceiver receiver;

    private OAuth2ClientStoring storing;

    public OAuth2ClientReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(OAuth2ClientReceiver receiver) {
        this.receiver = receiver;
    }

    public OAuth2ClientStoring getStoring() {
        return storing;
    }

    public void setStoring(OAuth2ClientStoring storing) {
        this.storing = storing;
    }

    public static class OAuth2ClientReceiver {

        private boolean enabled;

        private String host;

        private int port;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class OAuth2ClientStoring {

        private String location;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
