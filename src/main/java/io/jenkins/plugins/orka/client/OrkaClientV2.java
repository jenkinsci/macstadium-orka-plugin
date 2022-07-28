package io.jenkins.plugins.orka.client;

import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class OrkaClientV2 extends OrkaClient {
    private static final Map<String, TokenResponse> tokens = new HashMap<String, TokenResponse>();

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
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    protected TokenResponse getToken() {
        return tokens.get(this.getEmail());
    }

    @Override
    protected void initToken(String email, String password) throws IOException {
        if (!tokens.containsKey(email)) {
            TokenResponse tokenResponse = this.createToken(email, password);
            this.verifyToken(tokenResponse);
            tokens.put(email, tokenResponse);
        }
    }
}
