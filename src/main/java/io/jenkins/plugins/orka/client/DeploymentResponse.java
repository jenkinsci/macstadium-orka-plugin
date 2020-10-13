package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class DeploymentResponse extends ResponseBase {
    @SerializedName("ip")
    private String host;

    @SerializedName("ssh_port")
    private int sshPort;

    @SerializedName("vm_id")
    private String id;

    public DeploymentResponse(String host, int sshPort, String id, OrkaError[] errors, String message) {
        super(message, errors);
        this.host = host;
        this.sshPort = sshPort;
        this.id = id;
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

    @Override
    public String toString() {
        return "DeploymentResponse [errors=" + Arrays.toString(this.getErrors()) + ", host=" + host + ", id=" + id
                + ", message=" + this.getMessage() + ", sshPort=" + sshPort + "]";
    }
}
