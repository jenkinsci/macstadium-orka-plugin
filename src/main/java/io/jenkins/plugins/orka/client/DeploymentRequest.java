package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.lang.StringUtils;

public class DeploymentRequest {

    @SerializedName("orka_vm_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String vmName;

    @SerializedName("orka_node_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String node;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String scheduler;

    public DeploymentRequest(String vmName, String node) {
        this(vmName, node, null);
    }

    public DeploymentRequest(String vmName, String node, String scheduler) {
        this.vmName = vmName;
        this.node = node;
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
    }
}
