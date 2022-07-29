package io.jenkins.plugins.orka.client;

import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import okhttp3.Request;

public class OrkaClientV2 extends OrkaClient {
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final Logger logger = Logger.getLogger(OrkaClientV2.class.getName());
    private static final Map<String, TokenResponse> tokens = new HashMap<String, TokenResponse>();

    private String email;
    private String password;

    public OrkaClientV2(String endpoint, String email, String password) throws IOException {
        this(endpoint, email, password, defaultHttpClientTimeout, Proxy.NO_PROXY);
    }

    public OrkaClientV2(String endpoint, String email, String password, int httpClientTimeout, Proxy proxy)
            throws IOException {
        this(endpoint, email, password, httpClientTimeout, proxy, false);
    }

    public OrkaClientV2(String endpoint, String email, String password, int httpClientTimeout, Proxy proxy,
            boolean ignoreSSLErrors)
            throws IOException {
        super(endpoint, email, password);
        this.email = email;
        this.password = password;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    protected TokenResponse getToken() {
        return tokens.get(this.email);
    }

    private void setToken(TokenResponse token) {
        tokens.put(this.email, token);
    }

    @Override
    protected void initToken(String email, String password) throws IOException {
        if (!tokens.containsKey(email)) {
            this.initTokenImpl(email, password);
        }
    }

    private void initTokenImpl(String email, String password) throws IOException {
        TokenResponse tokenResponse = this.createToken(email, password);
        this.verifyToken(tokenResponse);
        this.setToken(tokenResponse);
    }

    @Override
    protected HttpResponse executeCall(Request request) throws IOException {
        HttpResponse response = executeCallImpl(request);

        if (response.getCode() == HTTP_UNAUTHORIZED) {
            logger.fine("Token was revoked. Create new token...");

            this.initTokenImpl(this.email, this.password);
            TokenResponse newToken = this.getToken();

            logger.fine("Retrying request with new token...");
            Request newRequest = request.newBuilder()
                    .removeHeader(AUTHORIZATION_HEADER)
                    .addHeader(AUTHORIZATION_HEADER, BEARER + newToken.getToken()).build();

            response = executeCallImpl(newRequest);
        }

        return response;
    }
}
