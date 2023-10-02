package io.jenkins.plugins.orka.client;

public class DeploymentResponse extends ResponseBase {
    private String ip;

    private int ssh;

    private String name;

    public DeploymentResponse(String ip, int ssh, String name, String message) {
        super(message);
        this.ip = ip;
        this.ssh = ssh;
        this.name = name;
    }

    public String getIP() {
        return this.ip;
    }

    public int getSSH() {
        return this.ssh;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "DeploymentResponse [IP=" + ip + ", name=" + name
                + ", message=" + this.getMessage() + ", ssh=" + ssh + "]";
    }
}
