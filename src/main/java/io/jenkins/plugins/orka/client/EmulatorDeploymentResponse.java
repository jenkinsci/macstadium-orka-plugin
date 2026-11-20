package io.jenkins.plugins.orka.client;

public class EmulatorDeploymentResponse extends ResponseBase {
    private String name;

    private String status;

    private String nodeName;

    private String relayIP;

    private int relayPort;

    public EmulatorDeploymentResponse(String name, String status, String nodeName, String relayIP, int relayPort,
            String message) {
        super(message);
        this.name = name;
        this.status = status;
        this.nodeName = nodeName;
        this.relayIP = relayIP;
        this.relayPort = relayPort;
    }

    public String getName() {
        return this.name;
    }

    public String getStatus() {
        return this.status;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public String getRelayIP() {
        return this.relayIP;
    }

    public int getRelayPort() {
        return this.relayPort;
    }

    @Override
    public String toString() {
        return "EmulatorDeploymentResponse [Name=" + name + ", Status=" + status + ", Node=" + nodeName
                + ", RelayIP=" + relayIP + ", RelayPort=" + relayPort + "]";
    }
}
