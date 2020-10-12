package io.jenkins.plugins.orka.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.AutoCloseable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import java.util.logging.Logger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrkaClient implements AutoCloseable {

    private static final int defaultHttpClientTimeout = 300;
    private static final OkHttpClient clientBase = new OkHttpClient();
    private static final Logger logger = Logger.getLogger(OrkaClient.class.getName());

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String TOKEN_PATH = "/token";
    private static final String RESOURCE_PATH = "/resources";
    private static final String VM_PATH = RESOURCE_PATH + "/vm";
    private static final String NODE_PATH = RESOURCE_PATH + "/node";
    private static final String IMAGE_PATH = RESOURCE_PATH + "/image";
    private static final String LIST_PATH = "/list";
    private static final String CREATE_PATH = "/create";
    private static final String DEPLOY_PATH = "/deploy";
    private static final String DELETE_PATH = "/delete";

    private String endpoint;
    private TokenResponse tokenResponse;
    private OkHttpClient client;

    public OrkaClient(String endpoint, String email, String password) throws IOException {
        this(endpoint, email, password, defaultHttpClientTimeout);
    }

    public OrkaClient(String endpoint, String email, String password, int httpClientTimeout) throws IOException {
        this.client = clientBase.newBuilder().readTimeout(httpClientTimeout, TimeUnit.SECONDS).build();
        this.endpoint = endpoint;
        this.tokenResponse = this.getToken(email, password);
    }

    public VMResponse getVMs() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + VM_PATH + LIST_PATH);

        Gson gson = new Gson();
        VMResponse response = gson.fromJson(httpResponse.getBody(), VMResponse.class);
        response.setHttpResponse(httpResponse);
        return response;
    }

    public NodeResponse getNodes() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + NODE_PATH + LIST_PATH);

        Gson gson = new Gson();
        NodeResponse response = gson.fromJson(httpResponse.getBody(), NodeResponse.class);
        response.setHttpResponse(httpResponse);
        return response;
    }

    public ImageResponse getImages() throws IOException {
        HttpResponse httpResponse = this.get(this.endpoint + IMAGE_PATH + LIST_PATH);
        Gson gson = new Gson();
        ImageResponse response = gson.fromJson(httpResponse.getBody(), ImageResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount) throws IOException {
        Gson gson = new Gson();

        ConfigurationRequest configRequest = new ConfigurationRequest(vmName, image, baseImage, configTemplate,
                cpuCount);

        String configRequestJson = gson.toJson(configRequest);

        HttpResponse httpResponse = this.post(this.endpoint + VM_PATH + CREATE_PATH, configRequestJson);
        ConfigurationResponse response = gson.fromJson(httpResponse.getBody(), ConfigurationResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public DeploymentResponse deployVM(String vmName) throws IOException {
        return this.deployVM(vmName, null);
    }

    public DeploymentResponse deployVM(String vmName, String node) throws IOException {
        Gson gson = new Gson();

        DeploymentRequest deploymentRequest = new DeploymentRequest(vmName, node);
        String deploymentRequestJson = gson.toJson(deploymentRequest);

        HttpResponse httpResponse = this.post(this.endpoint + VM_PATH + DEPLOY_PATH, deploymentRequestJson);
        DeploymentResponse response = gson.fromJson(httpResponse.getBody(), DeploymentResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public DeletionResponse deleteVM(String vmName) throws IOException {
        return this.deleteVM(vmName, null);
    }

    public DeletionResponse deleteVM(String vmName, String node) throws IOException {
        Gson gson = new Gson();

        DeletionRequest deletionRequest = new DeletionRequest(vmName, node);
        String deletionRequestJson = gson.toJson(deletionRequest);

        HttpResponse httpResponse = this.delete(this.endpoint + VM_PATH + DELETE_PATH, deletionRequestJson);
        DeletionResponse response = gson.fromJson(httpResponse.getBody(), DeletionResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public void close() throws IOException {
        this.delete(this.endpoint + TOKEN_PATH, "");
    }

    @VisibleForTesting
    TokenResponse getToken(String email, String password) throws IOException {
        TokenRequest tokenRequest = new TokenRequest(email, password);
        Gson gson = new Gson();
        String tokenRequestJson = new Gson().toJson(tokenRequest);

        HttpResponse httpResponse = this.post(this.endpoint + TOKEN_PATH, tokenRequestJson);
        TokenResponse response = gson.fromJson(httpResponse.getBody(), TokenResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    @VisibleForTesting
    HttpResponse post(String url, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, body);
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
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = this.getAuthenticatedBuilder(url).delete(requestBody).build();
        return executeCall(request);
    }

    private Builder getAuthenticatedBuilder(String url) throws IOException {
        return new Request.Builder().addHeader("Authorization", "Bearer " + this.tokenResponse.getToken()).url(url);
    }

    private HttpResponse executeCall(Request request) throws IOException {
        logger.fine("Executing request to Orka API: " + '/' + request.method() + ' ' + request.url());
        try (Response response = client.newCall(request).execute()) {
            return new HttpResponse(response.body().string(), response.code(), response.isSuccessful());
        }
    }
}
