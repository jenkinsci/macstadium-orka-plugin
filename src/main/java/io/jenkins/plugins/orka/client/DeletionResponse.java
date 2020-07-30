package io.jenkins.plugins.orka.client;

public class DeletionResponse extends ResponseBase {
    public DeletionResponse(String message, OrkaError[] errors) {
        super(message, errors);
    }
}
