package io.jenkins.plugins.orka.client;

public class ResponseBase {
    private HttpResponse httpResponse;

    private String message;

    public ResponseBase(String message) {
        this.message = message;
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

    public boolean isSuccessful() {
        return this.httpResponse != null ? this.httpResponse.getIsSuccessful() : true;
    }
}
