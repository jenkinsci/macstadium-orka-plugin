package io.jenkins.plugins.orka.client;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.lang.StringUtils;

public class DeploymentRequest {
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String vmConfig;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String node;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String scheduler;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String tag;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Boolean tagRequired;

    public DeploymentRequest(String vmConfig, String node, String scheduler,
            String tag, Boolean tagRequired) {
        this.vmConfig = vmConfig;
        this.node = node;
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
        this.tag = StringUtils.isNotBlank(tag) && tag != null ? tag : null;
        this.tagRequired = tagRequired != null ? tagRequired : null;
    }
}
