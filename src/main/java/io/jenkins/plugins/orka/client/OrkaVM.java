package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

public class OrkaVM {

    @SerializedName("virtual_machine_name")
    private String vmName;

    @SerializedName("vm_status")
    private String deploymentStatus;

    @SerializedName("cpu")
    private int cpuCount;

    @SerializedName("base_image")
    private String baseImage;

    @SerializedName("image")
    private String image;

    @SerializedName("configuration_template")
    private String configurationTemplate;

    private String memory;

    public OrkaVM(String vmName, String deploymentStatus, int cpuCount, String baseImage, String image,
            String configurationTemplate) {
        this(vmName, deploymentStatus, cpuCount, baseImage, image, configurationTemplate, "auto");
    }

    public OrkaVM(String vmName, String deploymentStatus, int cpuCount, String baseImage, String image,
            String configurationTemplate, String memory) {
        this.vmName = vmName;
        this.deploymentStatus = deploymentStatus;
        this.cpuCount = cpuCount;
        this.baseImage = baseImage;
        this.image = image;
        this.configurationTemplate = configurationTemplate;
        this.memory = memory;
    }

    public String getVMName() {
        return this.vmName;
    }

    public String getDeploymentStatus() {
        return this.deploymentStatus;
    }

    public int getCPUCount() {
        return this.cpuCount;
    }

    public String getBaseImage() {
        return this.baseImage;
    }

    public String getImage() {
        return this.image;
    }

    public String getConfigurationTemplate() {
        return this.configurationTemplate;
    }

    public String getMemory() {
        return this.memory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseImage == null) ? 0 : baseImage.hashCode());
        result = prime * result + ((configurationTemplate == null) ? 0 : configurationTemplate.hashCode());
        result = prime * result + cpuCount;
        result = prime * result + ((deploymentStatus == null) ? 0 : deploymentStatus.hashCode());
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + ((vmName == null) ? 0 : vmName.hashCode());
        result = prime * result + ((memory == null) ? 0 : memory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OrkaVM other = (OrkaVM) obj;
        if (baseImage == null) {
            if (other.baseImage != null) {
                return false;
            }
        } else if (!baseImage.equals(other.baseImage)) {
            return false;
        }
        if (configurationTemplate == null) {
            if (other.configurationTemplate != null) {
                return false;
            }
        } else if (!configurationTemplate.equals(other.configurationTemplate)) {
            return false;
        }
        if (cpuCount != other.cpuCount) {
            return false;
        }
        if (deploymentStatus == null) {
            if (other.deploymentStatus != null) {
                return false;
            }
        } else if (!deploymentStatus.equals(other.deploymentStatus)) {
            return false;
        }
        if (image == null) {
            if (other.image != null) {
                return false;
            }
        } else if (!image.equals(other.image)) {
            return false;
        }
        if (vmName == null) {
            if (other.vmName != null) {
                return false;
            }
        } else if (!vmName.equals(other.vmName)) {
            return false;
        }
        if (memory == null) {
            if (other.memory != null) {
                return false;
            }
        } else if (!memory.equals(other.memory)) {
            return false;
        }
        return true;
    }
}