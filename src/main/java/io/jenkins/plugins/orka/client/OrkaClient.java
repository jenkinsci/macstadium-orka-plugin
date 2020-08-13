package io.jenkins.plugins.orka.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.AutoCloseable;
import java.lang.reflect.Type;

import java.util.List;
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
    private static final OkHttpClient client = 
        new OkHttpClient.Builder().readTimeout(defaultHttpClientTimeout, TimeUnit.SECONDS).build();
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
    private String token;
    private int httpClientTimeout;

    public OrkaClient(String endpoint, String email, String password) throws IOException {
        this.setEndpointAndToken(endpoint, email, password);
        this.httpClientTimeout = defaultHttpClientTimeout;
    }
    
    public OrkaClient(String endpoint, String email, String password, int httpClientTimeout) throws IOException {
        this.setEndpointAndToken(endpoint, email, password);
        this.httpClientTimeout = httpClientTimeout > 0 ? httpClientTimeout : defaultHttpClientTimeout;
    }
    
    public List<VMResponse> getVMs() throws IOException {
        String response = this.get(this.endpoint + VM_PATH + LIST_PATH);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
        String responseJson = jsonObject.get("virtual_machine_resources").toString();

        Type listType = new TypeToken<List<VMResponse>>() {
        }.getType();

        return gson.fromJson(responseJson, listType);
    }

    public List<NodeResponse> getNodes() throws IOException {
        String response = this.get(this.endpoint + NODE_PATH + LIST_PATH);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
        String responseJson = jsonObject.get("nodes").toString();

        Type listType = new TypeToken<List<NodeResponse>>() {
        }.getType();

        return gson.fromJson(responseJson, listType);
    }

    public List<String> getImages() throws IOException {
        String response = this.get(this.endpoint + IMAGE_PATH + LIST_PATH);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
        String responseJson = jsonObject.get("images").toString();

        Type listType = new TypeToken<List<String>>() {
        }.getType();

        return gson.fromJson(responseJson, listType);
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount) throws IOException {
        Gson gson = new Gson();

        ConfigurationRequest configRequest = new ConfigurationRequest(vmName, image, baseImage, configTemplate,
                cpuCount);

        String configRequestJson = gson.toJson(configRequest);
        String response = this.post(this.endpoint + VM_PATH + CREATE_PATH, configRequestJson);

        return gson.fromJson(response, ConfigurationResponse.class);
    }

    public DeploymentResponse deployVM(String vmName) throws IOException {
        return this.deployVM(vmName, null);
    }

    public DeploymentResponse deployVM(String vmName, String node) throws IOException {
        Gson gson = new Gson();

        DeploymentRequest deploymentRequest = new DeploymentRequest(vmName, node);
        String deploymentRequestJson = gson.toJson(deploymentRequest);
        String response = this.post(this.endpoint + VM_PATH + DEPLOY_PATH, deploymentRequestJson);

        return gson.fromJson(response, DeploymentResponse.class);
    }

    public DeletionResponse deleteVM(String vmName) throws IOException {
        return this.deleteVM(vmName, null);
    }

    public DeletionResponse deleteVM(String vmName, String node) throws IOException {
        Gson gson = new Gson();

        DeletionRequest deletionRequest = new DeletionRequest(vmName, node);
        String deletionRequestJson = gson.toJson(deletionRequest);
        String response = this.delete(this.endpoint + VM_PATH + DELETE_PATH, deletionRequestJson);

        return gson.fromJson(response, DeletionResponse.class);
    }

    public void close() throws IOException {
        this.delete(this.endpoint + TOKEN_PATH, "");
    }

    @VisibleForTesting
    String getToken(String email, String password) throws IOException {
        TokenRequest tokenRequest = new TokenRequest(email, password);
        Gson gson = new Gson();
        String tokenRequestJson = new Gson().toJson(tokenRequest);

        String tokenResponseJson = this.post(this.endpoint + TOKEN_PATH, tokenRequestJson);
        TokenResponse response = gson.fromJson(tokenResponseJson, TokenResponse.class);

        return response.getToken();
    }

    @VisibleForTesting
    String post(String url, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = this.getAuthenticatedBuilder(url).post(requestBody).build();

        return executeCall(request);
    }

    @VisibleForTesting
    String get(String url) throws IOException {
        Request request = this.getAuthenticatedBuilder(url).get().build();
        return this.executeCall(request);
    }

    @VisibleForTesting
    String delete(String url, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = this.getAuthenticatedBuilder(url).delete(requestBody).build();
        return executeCall(request);
    }
    
    private void setEndpointAndToken(String endpoint, String email, String password) {
        this.endpoint = endpoint;
        this.token = this.getToken(email, password);
    }

    private Builder getAuthenticatedBuilder(String url) throws IOException {
        return new Request.Builder().addHeader("Authorization", "Bearer " + this.token).url(url);
    }

    private String executeCall(Request request) throws IOException {
        logger.fine("Executing request to Orka API: " + '/' + request.method() + ' ' + request.url());

        OkHttpClient httpClient = client;
        if (this.httpClientTimeout != defaultHttpClientTimeout) {
            httpClient = client.newBuilder().readTimeout(this.httpClientTimeout, TimeUnit.SECONDS).build();;
        } else {
            httpClient = client;
        }
        
        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
