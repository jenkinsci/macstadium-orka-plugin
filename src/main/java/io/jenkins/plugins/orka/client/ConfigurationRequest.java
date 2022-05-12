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

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String scheduler;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private int memory;

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount) {
        this(vmName, image, baseImage, configTemplate, cpuCount, null);
    }

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount,
            String scheduler) {
        this(vmName, image, baseImage, configTemplate, cpuCount, scheduler, "auto");
    }

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount,
            String scheduler, String memory) {
        this.vmName = vmName;
        this.image = image;
        this.baseImage = baseImage;
        this.configTemplate = configTemplate;
        this.cpuCount = cpuCount;
        this.scheduler = StringUtils.isNotBlank(scheduler) ? scheduler : null;
        if (!StringUtils.isBlank(memory) && !StringUtils.equals(memory, "auto") && Integer.parseInt(memory) > 0) {
            this.memory = Integer.parseInt(memory);
        }
    }
}
