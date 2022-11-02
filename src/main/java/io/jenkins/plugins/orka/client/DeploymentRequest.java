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

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String tag;

    @SerializedName("tag_required")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Boolean tagRequired;

    public DeploymentRequest(String vmName, String node) {
        this(vmName, node, null);
    }

    public DeploymentRequest(String vmName, String node, String scheduler) {
        this(vmName, node, scheduler, null, null);
    }

    public DeploymentRequest(String vmName, String node, String scheduler,
        String tag, Boolean tagRequired) {
        this.vmName = vmName;
        this.node = node;
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
        this.tag = StringUtils.isNotBlank(tag) && tag != null ? tag : null;
        this.tagRequired = tagRequired != null ? tagRequired : null;
    }
}
