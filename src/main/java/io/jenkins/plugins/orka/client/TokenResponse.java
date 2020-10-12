package io.jenkins.plugins.orka.client;

public class TokenResponse extends ResponseBase {
    private String token;

    public TokenResponse(String token, String message, OrkaError[] errors) {
        super(message, errors);
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
