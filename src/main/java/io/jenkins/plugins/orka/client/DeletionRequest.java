package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class DeletionRequest {
    @SerializedName("orka_vm_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String vmName;

    @SerializedName("orka_node_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String node;

    public DeletionRequest(String vmName, String node) {
        this.vmName = vmName;
        this.node = node;
    }
}
