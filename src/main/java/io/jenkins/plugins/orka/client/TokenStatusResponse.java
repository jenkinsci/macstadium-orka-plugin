package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

public class TokenStatusResponse extends ResponseBase {
    @SerializedName("authenticated")
    private boolean isValid;

    public TokenStatusResponse(boolean isValid, String message, OrkaError[] errors) {
        super(message, errors);
        this.isValid = isValid;
    }

    public boolean getIsValid() {
        return this.isValid;
    }
}
