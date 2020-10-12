package io.jenkins.plugins.orka.client;

public class ConfigurationResponse extends ResponseBase {
    public ConfigurationResponse(String message, OrkaError[] errors) {
        super(message, errors);
    }
}
