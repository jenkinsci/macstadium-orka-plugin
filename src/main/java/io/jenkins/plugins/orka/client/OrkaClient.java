package io.jenkins.plugins.orka.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;

import io.jenkins.plugins.orka.helpers.Utils;

import java.io.IOException;
import java.lang.AutoCloseable;
import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import java.util.logging.Logger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OrkaClient implements AutoCloseable {

    protected static final int defaultHttpClientTimeout = 600;
    private static final OkHttpClient clientBase = new OkHttpClient();
    private static final Logger logger = Logger.getLogger(OrkaClient.class.getName());

    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String BEARER = "Bearer ";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    protected static final String TOKEN_PATH = "/token";
    private static final String RESOURCE_PATH = "/resources";
    private static final String VM_PATH = RESOURCE_PATH + "/vm";
    private static final String NODE_PATH = RESOURCE_PATH + "/node";
    private static final String IMAGE_PATH = RESOURCE_PATH + "/image";
    private static final String LIST_PATH = "/list";
    private static final String CREATE_PATH = "/create";
    private static final String DEPLOY_PATH = "/deploy";
    private static final String DELETE_PATH = "/delete";
    private static final String CONFIG_PATH = "/configs";
    private static final String HEALTH_CHECK_PATH = "/health-check";

    private String endpoint;
    private TokenResponse tokenResponse;
    private OkHttpClient client;

    public OrkaClient(String endpoint, String email, String password) throws IOException {
        this(endpoint, email, password, defaultHttpClientTimeout, Proxy.NO_PROXY);
    }

    public OrkaClient(String endpoint, String email, String password, int httpClientTimeout, Proxy proxy)
            throws IOException {
        this(endpoint, email, password, httpClientTimeout, proxy, false);
    }

    public OrkaClient(String endpoint, String email, String password, int httpClientTimeout, Proxy proxy,
            boolean ignoreSSLErrors)
            throws IOException {
        this(endpoint, email, password, httpClientTimeout, proxy, ignoreSSLErrors, true);
    }

    public OrkaClient(String endpoint, String email, String password, int httpClientTimeout, Proxy proxy,
            boolean ignoreSSLErrors, boolean initToken)
            throws IOException {
        this.client = this.createClient(proxy, httpClientTimeout, ignoreSSLErrors);
        this.endpoint = endpoint;
        if (initToken) {
            this.initToken(email, password);
        }
    }

    public OrkaClient(String endpoint, int httpClientTimeout, Proxy proxy,
            boolean ignoreSSLErrors)
            throws IOException {
        this.client = this.createClient(proxy, httpClientTimeout, ignoreSSLErrors);
        this.endpoint = endpoint;
    }

    @VisibleForTesting
    TokenResponse getToken() {
        return this.tokenResponse;
    }

    public VMResponse getVMs() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + VM_PATH + LIST_PATH);

        VMResponse response = JsonHelper.fromJson(httpResponse.getBody(), VMResponse.class);
        response.setHttpResponse(httpResponse);
        return response;
    }

    public VMConfigResponse getVMConfigs() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + VM_PATH + CONFIG_PATH);

        VMConfigResponse response = JsonHelper.fromJson(httpResponse.getBody(), VMConfigResponse.class);
        response.setHttpResponse(httpResponse);
        return response;
    }

    public NodeResponse getNodes() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + NODE_PATH + LIST_PATH);

        NodeResponse response = JsonHelper.fromJson(httpResponse.getBody(), NodeResponse.class);
        response.setHttpResponse(httpResponse);
        return response;
    }

    public ImageResponse getImages() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + IMAGE_PATH + LIST_PATH);

        ImageResponse response = JsonHelper.fromJson(httpResponse.getBody(), ImageResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount) throws IOException {
        return this.createConfiguration(vmName, image, baseImage, configTemplate, cpuCount, null);
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount, String scheduler) throws IOException {
        return this.createConfiguration(vmName, image, baseImage, configTemplate, cpuCount, scheduler, "auto");
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount, String scheduler, String memory) throws IOException {
        return this.createConfiguration(vmName, image, baseImage, configTemplate, cpuCount, false, scheduler, "auto");
    }

    public ConfigurationResponse createConfiguration(
        String vmName, String image, String baseImage, String configTemplate, int cpuCount, boolean useNetBoost,
        String scheduler, String memory) throws IOException {

        ConfigurationRequest configRequest = new ConfigurationRequest(vmName, image, baseImage, configTemplate,
                cpuCount, useNetBoost, scheduler, memory);

        String configRequestJson = new Gson().toJson(configRequest);

        HttpResponse httpResponse = this.post(this.endpoint + VM_PATH + CREATE_PATH, configRequestJson);
        ConfigurationResponse response = JsonHelper.fromJson(httpResponse.getBody(), ConfigurationResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public DeploymentResponse deployVM(String vmName) throws IOException {
        return this.deployVM(vmName, null);
    }

    public DeploymentResponse deployVM(String vmName, String node) throws IOException {
        return this.deployVM(vmName, node, null);
    }

    public DeploymentResponse deployVM(String vmName, String node, String scheduler) throws IOException {
        DeploymentRequest deploymentRequest = new DeploymentRequest(vmName, node, scheduler);
        String deploymentRequestJson = new Gson().toJson(deploymentRequest);

        HttpResponse httpResponse = this.post(this.endpoint + VM_PATH + DEPLOY_PATH, deploymentRequestJson);
        DeploymentResponse response = JsonHelper.fromJson(httpResponse.getBody(), DeploymentResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public DeletionResponse deleteVM(String vmName) throws IOException {
        return this.deleteVM(vmName, null);
    }

    public DeletionResponse deleteVM(String vmName, String node) throws IOException {
        DeletionRequest deletionRequest = new DeletionRequest(vmName, node);
        String deletionRequestJson = new Gson().toJson(deletionRequest);

        HttpResponse httpResponse = this.delete(this.endpoint + VM_PATH + DELETE_PATH, deletionRequestJson);
        DeletionResponse response = JsonHelper.fromJson(httpResponse.getBody(), DeletionResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public HealthCheckResponse getHealthCheck() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + HEALTH_CHECK_PATH);
        HealthCheckResponse response = JsonHelper.fromJson(httpResponse.getBody(), HealthCheckResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public void close() throws IOException {
        if (this.getToken() != null) {
            this.delete(this.endpoint + TOKEN_PATH, "");
        }
    }

    protected void initToken(String email, String password) throws IOException {
        this.tokenResponse = this.createToken(email, password);
        this.verifyToken(this.tokenResponse);
    }

    @VisibleForTesting
    TokenResponse createToken(String email, String password) throws IOException {
        TokenRequest tokenRequest = new TokenRequest(email, password);
        String tokenRequestJson = new Gson().toJson(tokenRequest);

        HttpResponse httpResponse = this.post(this.endpoint + TOKEN_PATH, tokenRequestJson);
        TokenResponse response = JsonHelper.fromJson(httpResponse.getBody(), TokenResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    @VisibleForTesting
    HttpResponse post(String url, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(body, JSON);
        Request request = this.getAuthenticatedBuilder(url).post(requestBody).build();

        return executeCall(request);
    }

    @VisibleForTesting
    HttpResponse get(String url) throws IOException {
        Request request = this.getAuthenticatedBuilder(url).get().build();
        return this.executeCall(request);
    }

    @VisibleForTesting
    HttpResponse delete(String url, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(body, JSON);
        Request request = this.getAuthenticatedBuilder(url).delete(requestBody).build();
        return executeCall(request);
    }

    private Builder getAuthenticatedBuilder(String url) throws IOException {
        Request.Builder builder = new Request.Builder().url(url);
        TokenResponse tokenResponse = this.getToken();
        if (tokenResponse != null) {
            builder.addHeader(AUTHORIZATION_HEADER, BEARER + tokenResponse.getToken());
        }

        return builder;
    }

    protected HttpResponse executeCall(Request request) throws IOException {
        return executeCallImpl(request);
    }

    protected HttpResponse executeCallImpl(Request request) throws IOException {
        logger.fine("Executing request to Orka API: " + '/' + request.method() + ' ' + request.url());

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return new HttpResponse(body != null ? body.string() : null, response.code(), response.isSuccessful());
        }
    }

    void verifyToken(TokenResponse tokenResponse) throws IOException {
        if (!tokenResponse.isSuccessful()) {
            String error = String.format("Authentication failed with: %s", Utils.getErrorMessage(tokenResponse));
            throw new IOException(error);
        }
    }

    private OkHttpClient createClient(Proxy proxy, int httpClientTimeout, boolean ignoreSSLErrors) {
        OkHttpClient.Builder builder = ignoreSSLErrors ? SSLHelper.ignoreSSLErrors(clientBase.newBuilder())
                : clientBase.newBuilder();
        return builder.readTimeout(httpClientTimeout, TimeUnit.SECONDS).protocols(Arrays.asList(Protocol.HTTP_1_1))
                .proxy(proxy).build();
    }
}
