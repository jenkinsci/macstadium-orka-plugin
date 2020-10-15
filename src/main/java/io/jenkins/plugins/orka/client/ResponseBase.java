package io.jenkins.plugins.orka.client;

import java.util.Arrays;

public class ResponseBase {
    private HttpResponse httpResponse;

    private String message;

    private OrkaError[] errors;

    public ResponseBase(String message, OrkaError[] errors) {
        this.message = message;
        this.errors = errors != null ? errors.clone() : new OrkaError[] {};
    }

    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getMessage() {
        return this.message;
    }

    public OrkaError[] getErrors() {
        return this.errors.clone();
    }

    public boolean isSuccessful() {
        return this.httpResponse != null ? this.httpResponse.getIsSuccessful() : !this.hasErrors();
    }

    public String getErrorMessage() {
        if (!this.isSuccessful()) {
            return this.hasErrors() ? Arrays.toString(this.getErrors()) : httpResponse.getBody();
        }
        return null;
    }

    private boolean hasErrors() {
        return this.errors != null && this.errors.length > 0;
    }
}