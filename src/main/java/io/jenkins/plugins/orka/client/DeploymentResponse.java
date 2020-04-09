package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

public class DeploymentResponse {
    @SerializedName("ip")
    private String host;

    @SerializedName("ssh_port")
    private int sshPort;

    @SerializedName("vm_id")
    private String id;

    private OrkaError[] errors;

    private String message;

    public DeploymentResponse(String host, int sshPort, String id, OrkaError[] errors, String message) {
        this.host = host;
        this.sshPort = sshPort;
        this.id = id;
        this.errors = errors != null ? errors.clone() : new OrkaError[] {};
        this.message = message;
    }

    public String getHost() {
        return this.host;
    }

    public int getSSHPort() {
        return this.sshPort;
    }

    public String getId() {
        return this.id;
    }

    public OrkaError[] getErrors() {
        return this.errors.clone();
    }

    public String getMessage() {
        return this.message;
    }

    public boolean hasErrors() {
        return this.errors != null && this.errors.length > 0;
    }
}
