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

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String name;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private boolean shouldGenerateName;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String image;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Integer cpu;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private float memory;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Boolean legacyIO;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Boolean gpuPassthrough;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Boolean netBoost;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private int timeout;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String reservedPorts;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Integer displayWidth;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Integer displayHeight;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Integer displayDpi;

    @Deprecated
    public DeploymentRequest(String vmConfig, String name, String node, String scheduler,
            String tag, Boolean tagRequired) {
        this.vmConfig = vmConfig;
        this.node = node;
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
        this.tag = StringUtils.isNotBlank(tag) && tag != null ? tag : null;
        this.tagRequired = tagRequired != null ? tagRequired : null;
        this.name = name;
        this.shouldGenerateName = StringUtils.isNotBlank(this.name);
    }

    @Deprecated
    public DeploymentRequest(String vmConfig, String name, String image, Integer cpu, String memory, String node,
        String scheduler, String tag, Boolean tagRequired, Boolean netBoost, 
        Boolean legacyIO, Boolean gpuPassthrough) {
        this.vmConfig = vmConfig;
        this.node = node;
        this.image = image;
        this.cpu = cpu;
        if (!StringUtils.isBlank(memory) && !StringUtils.equals(memory, "auto") && Float.parseFloat(memory) > 0) {
            this.memory = Float.parseFloat(memory);
        }
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
        this.tag = StringUtils.isNotBlank(tag) && tag != null ? tag : null;
        this.tagRequired = tagRequired != null ? tagRequired : null;
        this.name = name;
        this.netBoost = netBoost;
        this.legacyIO = legacyIO;
        this.gpuPassthrough = gpuPassthrough;

        if (this.legacyIO) {
            this.netBoost = false;
        }

        this.shouldGenerateName = StringUtils.isNotBlank(this.name);
        this.timeout = 60 * 24; // Set the server timeout to a day
    }

    @Deprecated
    public DeploymentRequest(String vmConfig, String name, String image, Integer cpu, String memory, String node,
            String scheduler, String tag, Boolean tagRequired, Boolean netBoost, 
            Boolean legacyIO, Boolean gpuPassthrough, String portMappingsString) {
        this(vmConfig, name, image, cpu, memory, node, scheduler, tag, tagRequired, netBoost, 
            legacyIO, gpuPassthrough, portMappingsString, StringUtils.isNotBlank(name));
    }

    @Deprecated
    public DeploymentRequest(String vmConfig, String name, String image, Integer cpu, String memory, String node,
        String scheduler, String tag, Boolean tagRequired, Boolean netBoost, Boolean legacyIO, Boolean gpuPassthrough, 
        String portMappingsString, Boolean shouldGenerateName) {
        this(vmConfig, name, image, cpu, memory, node, scheduler, tag, tagRequired, netBoost, 
            legacyIO, gpuPassthrough, portMappingsString, shouldGenerateName, null, null, null);
    }

    public DeploymentRequest(String vmConfig, String name, String image, Integer cpu, String memory, String node,
        String scheduler, String tag, Boolean tagRequired, Boolean netBoost, Boolean legacyIO, Boolean gpuPassthrough, 
        String portMappingsString, Boolean shouldGenerateName, 
        Integer displayWidth, Integer displayHeight, Integer displayDpi) {
        this.vmConfig = vmConfig;
        this.node = node;
        this.image = image;
        this.cpu = cpu;
        if (!StringUtils.isBlank(memory) && !StringUtils.equals(memory, "auto") && Float.parseFloat(memory) > 0) {
            this.memory = Float.parseFloat(memory);
        }
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
        this.tag = StringUtils.isNotBlank(tag) && tag != null ? tag : null;
        this.tagRequired = tagRequired != null ? tagRequired : null;
        this.name = name;
        this.netBoost = netBoost;
        this.legacyIO = legacyIO;
        this.gpuPassthrough = gpuPassthrough;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.displayDpi = displayDpi;

        if (this.legacyIO) {
            this.netBoost = false;
        }
        this.shouldGenerateName = shouldGenerateName;
        this.timeout = 60 * 24; // Set the server timeout to a day

        this.reservedPorts = portMappingsString;
    }
}
