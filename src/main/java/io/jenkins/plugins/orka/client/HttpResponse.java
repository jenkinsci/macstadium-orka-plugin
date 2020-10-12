package io.jenkins.plugins.orka.client;

public class HttpResponse {
    private String body;
    private int code;
    private boolean isSuccessful;

    public HttpResponse(String body, int code, boolean isSuccessful) {
        this.body = body;
        this.code = code;
        this.isSuccessful = isSuccessful;
    }

    public String getBody() {
        return this.body;
    }

    public int getCode() {
        return this.code;
    }

    public boolean getIsSuccessful() {
        return this.isSuccessful;
    }
}