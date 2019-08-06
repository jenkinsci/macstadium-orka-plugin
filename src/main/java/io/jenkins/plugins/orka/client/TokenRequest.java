package io.jenkins.plugins.orka.client;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TokenRequest {

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String email;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String password;

    public TokenRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
