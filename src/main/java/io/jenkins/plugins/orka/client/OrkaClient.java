package io.jenkins.plugins.orka.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;

import java.io.IOException;
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

import org.apache.commons.lang.StringUtils;

public class OrkaClient {
    private static final OkHttpClient clientBase = new OkHttpClient();
    private static final Logger logger = Logger.getLogger(OrkaClient.class.getName());

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String RESOURCE_PATH = "api/v1/namespaces";
    private static final String VM_CONFIG_PATH = RESOURCE_PATH + "/orka-default/vmconfigs";
    private static final String VM_PATH = "vms";
    private static final String NODE_PATH = "nodes";
    private static final String IMAGE_PATH = RESOURCE_PATH + "/orka-default/images";

    private String endpoint;
    private String token;
    private OkHttpClient client;

    public OrkaClient(String endpoint, String token, int httpClientTimeout, Proxy proxy, boolean ignoreSSLErrors)
            throws IOException {
        this.client = this.createClient(proxy, httpClientTimeout, ignoreSSLErrors);
        this.endpoint = endpoint;
        this.token = token;
    }

    public VMConfigResponse getVMConfigs() throws IOException {
        HttpResponse httpResponse = this.get(String.format("%s/%s", this.endpoint, VM_CONFIG_PATH));

        VMConfigResponse response = JsonHelper.fromJson(httpResponse.getBody(), VMConfigResponse.class);
        response.setHttpResponse(httpResponse);
        return response;
    }

    public NodeResponse getNodes(String namespace) throws IOException {
        HttpResponse httpResponse = this
                .get(String.format("%s/%s/%s/%s", this.endpoint, RESOURCE_PATH, namespace, NODE_PATH));
        NodeResponse response = JsonHelper.fromJson(httpResponse.getBody(), NodeResponse.class);
        response.setHttpResponse(httpResponse);
        return response;
    }

    public ImageResponse getImages() throws IOException {
        HttpResponse httpResponse = this.get(String.format("%s/%s", this.endpoint, IMAGE_PATH));

        ImageResponse response = JsonHelper.fromJson(httpResponse.getBody(), ImageResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public ConfigurationResponse createConfiguration(
            String vmName, String image, int cpu, boolean netBoost, boolean gpuPassthrough, String scheduler,
            String memory, String tag, Boolean tagRequired) throws IOException {

        ConfigurationRequest configRequest = new ConfigurationRequest(vmName, image,
                cpu, netBoost, gpuPassthrough, scheduler, memory, tag, tagRequired);

        String configRequestJson = new Gson().toJson(configRequest);

        HttpResponse httpResponse = this.post(String.format("%s/%s", this.endpoint, VM_CONFIG_PATH), configRequestJson);
        ConfigurationResponse response = JsonHelper.fromJson(httpResponse.getBody(), ConfigurationResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public DeploymentResponse deployVM(String vmConfig, String namespace, String namePrefix, String image, Integer cpu,
            String memory, String node,
            String scheduler,
            String tag, Boolean tagRequired,Boolean netBoost, Boolean legacyIO, 
            Boolean gpuPassThrough, String portMappingString) throws IOException {
        DeploymentRequest deploymentRequest = new DeploymentRequest(vmConfig, namePrefix, image, cpu, memory, node,
                scheduler, tag, tagRequired, netBoost, legacyIO, gpuPassThrough, portMappingString);
        String deploymentRequestJson = new Gson().toJson(deploymentRequest);

        HttpResponse httpResponse = this.post(
                String.format("%s/%s/%s/%s", this.endpoint, RESOURCE_PATH, namespace, VM_PATH), deploymentRequestJson);
        DeploymentResponse response = JsonHelper.fromJson(httpResponse.getBody(), DeploymentResponse.class);
        response.setHttpResponse(httpResponse);

        return response;
    }

    public DeletionResponse deleteVM(String vmName, String namespace) throws IOException {
        HttpResponse httpResponse = this
                .delete(String.format("%s/%s/%s/%s/%s", this.endpoint, RESOURCE_PATH, namespace, VM_PATH, vmName));

        String body = httpResponse.getBody();
        DeletionResponse response;
        if (StringUtils.isNotBlank(body)) {
            response = JsonHelper.fromJson(httpResponse.getBody(), DeletionResponse.class);
        } else {
            response = new DeletionResponse(null);
        }
        response.setHttpResponse(httpResponse);

        return response;
    }

    public HealthCheckResponse getHealthCheck() throws IOException {
        HttpResponse httpResponse = this.get(String.format("%s/%s", this.endpoint, VM_CONFIG_PATH));
        HealthCheckResponse response = JsonHelper.fromJson(httpResponse.getBody(), HealthCheckResponse.class);
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
    HttpResponse delete(String url) throws IOException {
        Request request = this.getAuthenticatedBuilder(url).delete().build();
        return executeCall(request);
    }

    private Builder getAuthenticatedBuilder(String url) throws IOException {
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader(AUTHORIZATION_HEADER, BEARER + this.token);

        return builder;
    }

    private HttpResponse executeCall(Request request) throws IOException {
        return executeCallImpl(request);
    }

    private HttpResponse executeCallImpl(Request request) throws IOException {
        logger.fine("Executing request to Orka API: " + '/' + request.method() + ' ' + request.url());

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return new HttpResponse(body != null ? body.string() : null, response.code(), response.isSuccessful());
        }
    }

    private OkHttpClient createClient(Proxy proxy, int httpClientTimeout, boolean ignoreSSLErrors) {
        OkHttpClient.Builder builder = ignoreSSLErrors ? SSLHelper.ignoreSSLErrors(clientBase.newBuilder())
                : clientBase.newBuilder();
        return builder.readTimeout(httpClientTimeout, TimeUnit.SECONDS).protocols(Arrays.asList(Protocol.HTTP_1_1))
                .proxy(proxy).build();
    }
}
