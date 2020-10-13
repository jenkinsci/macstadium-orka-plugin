package io.jenkins.plugins.orka.client;

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

    public boolean hasErrors() {
        return this.errors != null && this.errors.length > 0;
    }
}