package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.lang.StringUtils;

public class ConfigurationRequest {

    @SerializedName("orka_vm_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String vmName;

    @SerializedName("orka_image")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String image;

    @SerializedName("orka_base_image")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String baseImage;

    @SerializedName("orka_vm_config_template_name")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String configTemplate;

    @SerializedName("orka_cpu_core")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private int cpuCount;

    @SerializedName("net_boost")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private boolean useNetBoost;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String scheduler;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private float memory;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String tag;

    @SerializedName("tag_required")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Boolean tagRequired;

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount) {
        this(vmName, image, baseImage, configTemplate, cpuCount, null);
    }

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount,
            String scheduler) {
        this(vmName, image, baseImage, configTemplate, cpuCount, scheduler, "auto");
    }

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount,
            String scheduler, String memory) {
        this(vmName, image, baseImage, configTemplate, cpuCount, false, scheduler, memory);
    }

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount,
            boolean useNetBoost, String scheduler, String memory) {
        this(vmName, image, baseImage, configTemplate, cpuCount, useNetBoost, scheduler, memory, null, null);
    }

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount,
            boolean useNetBoost, String scheduler, String memory, String tag, Boolean tagRequired) {
        this.vmName = vmName;
        this.image = image;
        this.baseImage = baseImage;
        this.configTemplate = configTemplate;
        this.cpuCount = cpuCount;
        this.useNetBoost = useNetBoost;
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
        if (!StringUtils.isBlank(memory) && !StringUtils.equals(memory, "auto") && Float.parseFloat(memory) > 0) {
            this.memory = Float.parseFloat(memory);
        }
        this.tag = StringUtils.isNotBlank(tag) && tag != null ? tag : null;
        this.tagRequired = tagRequired != null ? tagRequired : null;
    }
}
