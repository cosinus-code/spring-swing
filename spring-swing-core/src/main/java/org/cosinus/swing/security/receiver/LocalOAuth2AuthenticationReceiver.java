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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.cosinus.swing.action.browser.Browser;
import org.cosinus.swing.action.browser.DefaultBrowser;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.*;

public class LocalOAuth2AuthenticationReceiver implements OAuth2AuthenticationReceiver, AutoCloseable {

    private final Browser browser = new DefaultBrowser();

    private HttpServer server;

    private String authorizationCode;

    private String errorCode;

    private String state;

    private String scope;

    final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);

    private int port;

    private final String host;

    private final String callbackPath;

    private String redirectUri;

    private final String successLandingPageUrl;

    private final String failureLandingPageUrl;

    public LocalOAuth2AuthenticationReceiver() {
        this(LOCALHOST, -1, CALLBACK_PATH, null, null);
    }

    LocalOAuth2AuthenticationReceiver(
        String host, int port, String successLandingPageUrl, String failureLandingPageUrl) {
        this(host, port, CALLBACK_PATH, successLandingPageUrl, failureLandingPageUrl);
    }

    LocalOAuth2AuthenticationReceiver(
        String host,
        int port,
        String callbackPath,
        String successLandingPageUrl,
        String failureLandingPageUrl) {
        this.host = host;
        this.port = port;
        this.callbackPath = callbackPath;
        this.successLandingPageUrl = successLandingPageUrl;
        this.failureLandingPageUrl = failureLandingPageUrl;
    }


    @Override
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port != -1 ? port : findOpenPort()), 0);
        server.createContext(callbackPath, new CallbackHandler());
        server.setExecutor(null);

        server.start();
        port = server.getAddress().getPort();

        this.redirectUri = buildUrl(host, port, callbackPath);
    }

    public static String buildUrl(String host, int port) {
        return buildUrl(host, port, CALLBACK_PATH);
    }

    public static String buildUrl(String host, int port, String path) {
        return REDIRECT_URI_PATTERN.formatted(host, port, path);
    }

    @Override
    public OAuth2AuthorizationResponse execute(final OAuth2AuthorizationRequest authorizationRequest)
        throws IOException {

        browser.browse(authorizationRequest.getAuthorizationRequestUri());
        waitForResponse();

        // TODO: to extract from response
        String errorDescription = null;
        String errorUri = null;

        return isSuccess() ?
            OAuth2AuthorizationResponse
                .success(getAuthorizationCode())
                .redirectUri(getRedirectUri())
                .state(getState())
                .build() :
            OAuth2AuthorizationResponse
                .error(getErrorCode())
                .errorDescription(errorDescription)
                .errorUri(errorUri)
                .redirectUri(getRedirectUri())
                .state(getState())
                .build();
    }

    public void waitForResponse() throws IOException {
        waitUnlessSignaled.acquireUninterruptibly();
        if (errorCode != null) {
            throw new IOException("User authorization failed (" + errorCode + ")");
        }
    }

    public boolean isSuccess() {
        return authorizationCode != null;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getState() {
        return state;
    }

    @Override
    public void stop() throws IOException {
        waitUnlessSignaled.release();
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private int findOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("No free TCP/IP port to start embedded HTTP Server on");
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public static LocalOAuth2AuthenticationReceiver callbackReceiver(String host, int port) {
        return new LocalOAuth2AuthenticationReceiver
            .Builder()
            .setHost(host)
            .setPort(port)
            .build();
    }

    public static final class Builder {

        private String host = LOCALHOST;

        private int port = -1;

        private String successLandingPageUrl;
        private String failureLandingPageUrl;

        private String callbackPath = CALLBACK_PATH;

        public LocalOAuth2AuthenticationReceiver build() {
            return new LocalOAuth2AuthenticationReceiver(
                host, port, callbackPath, successLandingPageUrl, failureLandingPageUrl);
        }

        public String getHost() {
            return host;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public String getCallbackPath() {
            return callbackPath;
        }

        public Builder setCallbackPath(String callbackPath) {
            this.callbackPath = callbackPath;
            return this;
        }

        public Builder setLandingPages(String successLandingPageUrl, String failureLandingPageUrl) {
            this.successLandingPageUrl = successLandingPageUrl;
            this.failureLandingPageUrl = failureLandingPageUrl;
            return this;
        }
    }

    public class CallbackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            if (!callbackPath.equals(httpExchange.getRequestURI().getPath())) {
                return;
            }

            try {
                Map<String, String> parms = this.toQueryMap(httpExchange.getRequestURI().getQuery());
                errorCode = parms.get(ERROR);
                authorizationCode = parms.get(CODE);
                state = parms.get(STATE);
                scope = parms.get(SCOPE);

                Headers responseHeaders = httpExchange.getResponseHeaders();

                String landingPage = isSuccess() ? successLandingPageUrl : failureLandingPageUrl;
                if (landingPage != null) {
                    responseHeaders.add(LOCATION, landingPage);
                    httpExchange.sendResponseHeaders(HTTP_MOVED_TEMP, -1);
                } else {
                    writeLandingHtml(httpExchange, responseHeaders);
                }
                httpExchange.close();
            } finally {
                waitUnlessSignaled.release();
            }
        }

        private Map<String, String> toQueryMap(String queryParams) {
            return ofNullable(queryParams)
                .map(query -> query.split("&"))
                .stream()
                .flatMap(Arrays::stream)
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                    pair -> pair[0],
                    pair -> pair.length > 1 ? pair[1] : ""));
        }

        private void writeLandingHtml(HttpExchange exchange, Headers headers) throws IOException {
            try (OutputStream outputStream = exchange.getResponseBody()) {
                exchange.sendResponseHeaders(HTTP_OK, 0);
                headers.add(CONTENT_TYPE, TEXT_HTML_VALUE);

                OutputStreamWriter doc = new OutputStreamWriter(outputStream, UTF_8);
                doc.write("<html>");
                doc.write("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
                doc.write("<body>");
                doc.write("Received verification code. You may now close this window.");
                doc.write("</body>");
                doc.write("</html>\n");
                doc.flush();
            }
        }
    }
}
