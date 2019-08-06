package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    public ConfigurationRequest(String vmName, String image, String baseImage, String configTemplate, int cpuCount) {
        this.vmName = vmName;
        this.image = image;
        this.baseImage = baseImage;
        this.configTemplate = configTemplate;
        this.cpuCount = cpuCount;
    }
}
