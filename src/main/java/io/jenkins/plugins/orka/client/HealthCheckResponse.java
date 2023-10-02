package io.jenkins.plugins.orka.client;

public class HealthCheckResponse extends ResponseBase {
    public HealthCheckResponse(String apiVersion, String message) {
        super(message);
    }
}
