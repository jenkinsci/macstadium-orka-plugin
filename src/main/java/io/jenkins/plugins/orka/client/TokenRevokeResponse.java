package io.jenkins.plugins.orka.client;

public class TokenRevokeResponse extends ResponseBase {
    private int tokensRevoked;

    public TokenRevokeResponse(int tokensRevoked, String message, OrkaError[] errors) {
        super(message, errors);
        this.tokensRevoked = tokensRevoked;
    }

    public int getTokensRevoked() {
        return this.tokensRevoked;
    }
}
