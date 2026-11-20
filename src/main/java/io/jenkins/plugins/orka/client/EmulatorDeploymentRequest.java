package io.jenkins.plugins.orka.client;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.lang.StringUtils;

public class EmulatorDeploymentRequest {
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String vmName;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String config;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String name;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String adbPublicKey;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Integer timeout;

    public EmulatorDeploymentRequest(String vmName, String config, String name, String adbPublicKey,
            Integer timeout) {
        this.vmName = vmName;
        this.config = config;
        this.name = name;
        this.adbPublicKey = StringUtils.isNotBlank(adbPublicKey) ? adbPublicKey.trim() : null;
        this.timeout = timeout != null && timeout > 0 ? timeout : null;
    }
}
