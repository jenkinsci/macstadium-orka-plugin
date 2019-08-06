package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class DeploymentRequest {

    @SerializedName("orka_vm_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String vmName;

    @SerializedName("orka_node_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String node;

    public DeploymentRequest(String vmName, String node) {
        this.vmName = vmName;
        this.node = node;
    }
}
