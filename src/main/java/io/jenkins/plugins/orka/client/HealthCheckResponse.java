package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

public class HealthCheckResponse extends ResponseBase {
    @SerializedName("api_version")
    private String apiVersion;

    public HealthCheckResponse(String apiVersion, String message, OrkaError[] errors) {
        super(message, errors);
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }
}
